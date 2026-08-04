package com.krince.reminisce.infra.adapter.`in`.controller

import com.krince.reminisce.infra.adapter.out.persistence.user.entity.UserOrmEntity
import com.krince.reminisce.shared.response.ExceptionResponseCode
import com.krince.reminisce.shared.response.SuccessResponseCode
import com.krince.reminisce.testutil.TestConfig
import com.krince.reminisce.testutil.fixture.TestJwtTokenFixture
import com.krince.reminisce.testutil.fixture.TestUserFixture
import io.kotest.common.ExperimentalKotest
import io.kotest.core.annotation.DisplayName
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldNotContain
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.parsing.Parser
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasKey
import org.hamcrest.Matchers.nullValue
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@OptIn(ExperimentalKotest::class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("localtest")
@Import(TestConfig::class)
@io.kotest.core.annotation.Tags("test", "integrationTest")
@DisplayName("UserControllerImpl 통합테스트")
class UserControllerImplTest(
    @param:LocalServerPort private val port: Int,
    private val testUserFixture: TestUserFixture,
    private val testJwtTokenFixture: TestJwtTokenFixture,
) : FunSpec({

    fun localUserEntity(userId: String, email: String): UserOrmEntity =
        UserOrmEntity(
            userId = userId,
            email = email,
            password = "\$2a\$10\$hashedvaluehashedvaluehashedvalue",
            nickname = "홍길동",
            provider = "LOCAL",
            role = "ROLE_USER",
        )

    fun kakaoUserEntity(userId: String, providerId: String): UserOrmEntity =
        UserOrmEntity(
            userId = userId,
            email = null,
            password = null,
            nickname = "카카오",
            provider = "KAKAO",
            role = "ROLE_USER",
            providerId = providerId,
        )

    fun uniqueSuffix(): String = "${System.currentTimeMillis()}-${System.nanoTime()}"

    beforeSpec {
        RestAssured.port = port
        RestAssured.basePath = "/api"
        RestAssured.defaultParser = Parser.JSON
    }

    beforeTest {
        testUserFixture.deleteAllBatch()
    }

    context("getUser") {
        context("성공") {
            test("유효한 토큰으로 /me를 조회하면 200과 본인 정보를 반환한다") {
                val userId = "user-${uniqueSuffix()}"
                val email = "user${uniqueSuffix()}@example.com"
                val savedUser = testUserFixture.saveUser(localUserEntity(userId, email))
                val token = testJwtTokenFixture.generateAccessToken(savedUser.userId, savedUser.role)

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .`when`()
                    .get("/users/me")
                    .then()
                    .statusCode(200)
                    .body("success", equalTo(true))
                    .body("code", equalTo(200))
                    .body("message", equalTo(SuccessResponseCode.OK.message))
                    .body("data", hasKey("id"))
                    .body("data", hasKey("email"))
                    .body("data", hasKey("nickname"))
                    .body("data", hasKey("role"))
                    .body("data", hasKey("createdDate"))
                    .body("data", hasKey("modifiedDate"))
                    .body("data.id", equalTo(userId))
                    .body("data.email", equalTo(email))
                    .body("data.nickname", equalTo("홍길동"))
                    .body("data.role", equalTo("ROLE_USER"))
            }

            test("email이 null인 카카오 계정 토큰으로 /me를 조회하면 200과 email null을 반환한다") {
                val userId = "kakao-${uniqueSuffix()}"
                val savedUser = testUserFixture.saveUser(kakaoUserEntity(userId, "kakao-${uniqueSuffix()}"))
                val token = testJwtTokenFixture.generateAccessToken(savedUser.userId, savedUser.role)

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .`when`()
                    .get("/users/me")
                    .then()
                    .statusCode(200)
                    .body("success", equalTo(true))
                    .body("code", equalTo(200))
                    .body("data.id", equalTo(userId))
                    .body("data.email", nullValue())
                    .body("data.nickname", equalTo("카카오"))
                    .body("data.role", equalTo("ROLE_USER"))
            }
        }
        context("예외케이스") {
            test("토큰이 없으면 401과 EMPTY_TOKEN을 반환한다") {
                val userId = "user-${uniqueSuffix()}"
                testUserFixture.saveUser(localUserEntity(userId, "empty$userId@example.com"))

                RestAssured.given()
                    .contentType(ContentType.JSON)
                    .`when`()
                    .get("/users/me")
                    .then()
                    .statusCode(401)
                    .body("success", equalTo(false))
                    .body("code", equalTo(401))
                    .body("detailCode", equalTo(ExceptionResponseCode.EMPTY_TOKEN.detailCode))
                    .body("message", equalTo("토큰이 없습니다."))
            }

            test("타인의 userId 경로로 조회하면 매핑이 사라져 404 NOT_FOUND를 반환하고 타인 정보를 노출하지 않는다") {
                val userId = "user-${uniqueSuffix()}"
                val savedUser = testUserFixture.saveUser(localUserEntity(userId, "owner${uniqueSuffix()}@example.com"))
                val token = testJwtTokenFixture.generateAccessToken(savedUser.userId, savedUser.role)
                val otherUserId = "other-${uniqueSuffix()}"
                val otherEmail = "other${uniqueSuffix()}@example.com"
                testUserFixture.saveUser(localUserEntity(otherUserId, otherEmail))

                val response = RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .`when`()
                    .get("/users/$otherUserId")
                    .then()
                    .statusCode(404)
                    .body("success", equalTo(false))
                    .body("code", equalTo(404))
                    .body("detailCode", equalTo(ExceptionResponseCode.NOT_FOUND.detailCode))
                    .extract()

                response.body().asString() shouldNotContain otherEmail
                response.body().asString() shouldNotContain otherUserId
            }

            test("유효하지 않은 토큰이면 401과 INVALID_TOKEN을 반환한다") {
                RestAssured.given()
                    .header("Authorization", "Bearer invalid.token.value")
                    .contentType(ContentType.JSON)
                    .`when`()
                    .get("/users/me")
                    .then()
                    .statusCode(401)
                    .body("success", equalTo(false))
                    .body("code", equalTo(401))
                    .body("detailCode", equalTo(ExceptionResponseCode.INVALID_TOKEN.detailCode))
                    .body("message", equalTo("유효하지 않은 토큰입니다."))
            }
        }
    }
})
