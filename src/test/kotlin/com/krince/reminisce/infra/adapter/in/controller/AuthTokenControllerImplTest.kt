package com.krince.reminisce.infra.adapter.`in`.controller

import com.krince.reminisce.shared.response.ExceptionResponseCode
import com.krince.reminisce.testutil.TestConfig
import com.krince.reminisce.testutil.fixture.TestAuthUserFixture
import com.krince.reminisce.testutil.fixture.TestUserFixture
import io.kotest.common.ExperimentalKotest
import io.kotest.core.annotation.DisplayName
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.parsing.Parser
import org.hamcrest.Matchers.equalTo
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.test.context.ActiveProfiles

@OptIn(ExperimentalKotest::class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("localtest")
@Import(TestConfig::class)
@io.kotest.core.annotation.Tags("test", "integrationTest")
@DisplayName("로그인·토큰 통합테스트")
class AuthTokenControllerImplTest(
    @param:LocalServerPort private val port: Int,
    private val testUserFixture: TestUserFixture,
    private val testAuthUserFixture: TestAuthUserFixture,
    private val redisTemplate: StringRedisTemplate,
) : FunSpec({

    val rawPassword = "Password1!"

    fun uniqueEmail(prefix: String): String = "$prefix${System.nanoTime()}@example.com"

    fun storedRefresh(userId: String): String? = redisTemplate.opsForValue().get("auth:refresh:$userId")

    fun login(email: String, password: String) =
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(mapOf("email" to email, "password" to password))
            .`when`()
            .post("/auth/tokens")
            .then()

    beforeSpec {
        RestAssured.port = port
        RestAssured.basePath = "/api"
        RestAssured.defaultParser = Parser.JSON
    }

    beforeTest {
        testUserFixture.deleteAllBatch()
        redisTemplate.connectionFactory?.connection?.serverCommands()?.flushAll()
    }

    context("로그인") {
        test("올바른 자격증명이면 200과 Authorization·refreshToken 헤더를 주고 Redis에 리프레시를 저장한다") {
            val email = uniqueEmail("login")
            val userId = testAuthUserFixture.saveLocalUser(email, rawPassword)

            val response = login(email, rawPassword)
                .statusCode(200)
                .extract()

            val accessHeader = response.header("Authorization")
            val refreshHeader = response.header("refreshToken")
            accessHeader.shouldNotBeNull()
            accessHeader shouldStartWith "Bearer "
            refreshHeader.shouldNotBeNull()
            refreshHeader shouldStartWith "Bearer "
            storedRefresh(userId) shouldBe refreshHeader
        }
        test("존재하지 않는 이메일이면 401과 INVALID_PASSWORD를 반환한다") {
            login(uniqueEmail("nouser"), rawPassword)
                .statusCode(401)
                .body("detailCode", equalTo(ExceptionResponseCode.INVALID_PASSWORD.detailCode))
        }
        test("비밀번호가 틀리면 없는 이메일과 동일한 401·INVALID_PASSWORD를 반환한다(사용자 열거 불가)") {
            val email = uniqueEmail("wrongpw")
            testAuthUserFixture.saveLocalUser(email, rawPassword)

            login(email, "Wrongpass9!")
                .statusCode(401)
                .body("detailCode", equalTo(ExceptionResponseCode.INVALID_PASSWORD.detailCode))
        }
    }

    context("토큰 재발급") {
        test("유효한 리프레시로 재발급하면 새 헤더를 주고 Redis 저장분을 교체하며 기존 리프레시는 거부한다") {
            val email = uniqueEmail("reissue")
            val userId = testAuthUserFixture.saveLocalUser(email, rawPassword)

            val oldRefresh = login(email, rawPassword).statusCode(200).extract().header("refreshToken")
            oldRefresh.shouldNotBeNull()

            val reissued = RestAssured.given()
                .header("refreshToken", oldRefresh)
                .`when`()
                .post("/auth/tokens/refresh")
                .then()
                .statusCode(200)
                .extract()

            val newRefresh = reissued.header("refreshToken")
            val newAccess = reissued.header("Authorization")
            newAccess.shouldNotBeNull()
            newAccess shouldStartWith "Bearer "
            newRefresh.shouldNotBeNull()
            (newRefresh == oldRefresh) shouldBe false
            storedRefresh(userId) shouldBe newRefresh

            RestAssured.given()
                .header("refreshToken", oldRefresh)
                .`when`()
                .post("/auth/tokens/refresh")
                .then()
                .statusCode(402)
                .body("detailCode", equalTo(ExceptionResponseCode.INVALID_REFRESH_TOKEN.detailCode))
        }
        test("리프레시 토큰 헤더가 없으면 402와 EMPTY_REFRESH_TOKEN을 반환한다") {
            RestAssured.given()
                .`when`()
                .post("/auth/tokens/refresh")
                .then()
                .statusCode(402)
                .body("detailCode", equalTo(ExceptionResponseCode.EMPTY_REFRESH_TOKEN.detailCode))
        }
        test("Redis에 없는 리프레시로 재발급하면 402를 반환한다") {
            val email = uniqueEmail("stale")
            testAuthUserFixture.saveLocalUser(email, rawPassword)

            val refresh = login(email, rawPassword).statusCode(200).extract().header("refreshToken")
            refresh.shouldNotBeNull()
            redisTemplate.connectionFactory?.connection?.serverCommands()?.flushAll()

            RestAssured.given()
                .header("refreshToken", refresh)
                .`when`()
                .post("/auth/tokens/refresh")
                .then()
                .statusCode(402)
                .body("detailCode", equalTo(ExceptionResponseCode.INVALID_REFRESH_TOKEN.detailCode))
        }
    }

    context("로그아웃") {
        test("로그아웃하면 Redis 저장분을 삭제하고 그 리프레시로 재발급하면 거부한다") {
            val email = uniqueEmail("logout")
            val userId = testAuthUserFixture.saveLocalUser(email, rawPassword)

            val refresh = login(email, rawPassword).statusCode(200).extract().header("refreshToken")
            refresh.shouldNotBeNull()
            storedRefresh(userId).shouldNotBeNull()

            RestAssured.given()
                .header("refreshToken", refresh)
                .`when`()
                .delete("/auth/tokens")
                .then()
                .statusCode(204)

            storedRefresh(userId) shouldBe null

            RestAssured.given()
                .header("refreshToken", refresh)
                .`when`()
                .post("/auth/tokens/refresh")
                .then()
                .statusCode(402)
                .body("detailCode", equalTo(ExceptionResponseCode.INVALID_REFRESH_TOKEN.detailCode))
        }
    }
})
