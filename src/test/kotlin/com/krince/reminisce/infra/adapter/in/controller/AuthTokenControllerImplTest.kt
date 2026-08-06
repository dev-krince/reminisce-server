package com.krince.reminisce.infra.adapter.`in`.controller

import com.krince.reminisce.infra.security.JwtProvider
import com.krince.reminisce.shared.response.ExceptionResponseCode
import com.krince.reminisce.testutil.TestConfig
import com.krince.reminisce.testutil.fixture.TestAuthUserFixture
import com.krince.reminisce.testutil.fixture.TestJwtTokenFixture
import com.krince.reminisce.testutil.fixture.TestUserFixture
import io.kotest.common.ExperimentalKotest
import io.kotest.core.annotation.DisplayName
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import io.restassured.RestAssured
import io.restassured.parsing.Parser
import org.hamcrest.Matchers.equalTo
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.test.context.ActiveProfiles
import java.time.Duration

@OptIn(ExperimentalKotest::class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("localtest")
@Import(TestConfig::class)
@io.kotest.core.annotation.Tags("test", "integrationTest")
@DisplayName("토큰 재발급·로그아웃 통합테스트")
class AuthTokenControllerImplTest(
    @param:LocalServerPort private val port: Int,
    @param:Value("\${jwt.access-token-expired}") private val accessTokenExpiredMillis: Long,
    private val testUserFixture: TestUserFixture,
    private val testAuthUserFixture: TestAuthUserFixture,
    private val testJwtTokenFixture: TestJwtTokenFixture,
    private val redisTemplate: StringRedisTemplate,
    private val jwtProvider: JwtProvider,
) : FunSpec({

    val millisPerSecond = 1000L
    val accessTokenExpiredSeconds = accessTokenExpiredMillis / millisPerSecond
    val refreshTtl = Duration.ofMinutes(30)

    fun uniqueSuffix(): String = "${System.currentTimeMillis()}-${System.nanoTime()}"

    fun storedRefresh(userId: String): String? = redisTemplate.opsForValue().get("auth:refresh:$userId")

    fun issueSession(userId: String): Pair<String, String> {
        val access = testJwtTokenFixture.generateAccessToken(userId)
        val refresh = testJwtTokenFixture.generateRefreshToken(userId)
        redisTemplate.opsForValue().set("auth:refresh:$userId", refresh, refreshTtl)

        return access to refresh
    }

    fun accessTokenId(bearerAccessToken: String): String =
        requireNotNull(jwtProvider.getTokenId(bearerAccessToken.removePrefix("Bearer ").trim()))

    fun blacklistExpire(tokenId: String): Long? = redisTemplate.getExpire("auth:blacklist:$tokenId")

    fun blacklisted(tokenId: String): Boolean = redisTemplate.hasKey("auth:blacklist:$tokenId")

    beforeSpec {
        RestAssured.port = port
        RestAssured.basePath = "/api"
        RestAssured.defaultParser = Parser.JSON
    }

    beforeTest {
        testUserFixture.deleteAllBatch()
        redisTemplate.connectionFactory?.connection?.serverCommands()?.flushAll()
    }

    context("토큰 재발급") {
        test("유효한 리프레시로 재발급하면 새 헤더를 주고 Redis 저장분을 교체하며 기존 리프레시는 거부한다") {
            val userId = testAuthUserFixture.saveKakaoUser("kakao-${uniqueSuffix()}")
            val (_, oldRefresh) = issueSession(userId)

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
            val userId = testAuthUserFixture.saveKakaoUser("kakao-${uniqueSuffix()}")
            val (_, refresh) = issueSession(userId)
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
            val userId = testAuthUserFixture.saveKakaoUser("kakao-${uniqueSuffix()}")
            val (_, refresh) = issueSession(userId)
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
        test("액세스+리프레시로 로그아웃하면 같은 액세스로 인증 API 재요청이 401 LOGGED_OUT_TOKEN이고 블랙리스트 TTL이 액세스 만료 이하이다") {
            val userId = testAuthUserFixture.saveKakaoUser("kakao-${uniqueSuffix()}")
            val (access, refresh) = issueSession(userId)

            RestAssured.given()
                .header("Authorization", access)
                .`when`()
                .get("/users/me")
                .then()
                .statusCode(200)

            RestAssured.given()
                .header("Authorization", access)
                .header("refreshToken", refresh)
                .`when`()
                .delete("/auth/tokens")
                .then()
                .statusCode(204)

            val tokenId = accessTokenId(access)
            blacklisted(tokenId) shouldBe true
            val expire = blacklistExpire(tokenId)
            expire.shouldNotBeNull()
            (expire > 0) shouldBe true
            (expire <= accessTokenExpiredSeconds) shouldBe true

            RestAssured.given()
                .header("Authorization", access)
                .`when`()
                .get("/users/me")
                .then()
                .statusCode(401)
                .body("detailCode", equalTo(ExceptionResponseCode.LOGGED_OUT_TOKEN.detailCode))
        }
        test("액세스 헤더 없이 로그아웃하면 리프레시만 삭제하고 블랙리스트에는 아무것도 등록하지 않는다") {
            val userId = testAuthUserFixture.saveKakaoUser("kakao-${uniqueSuffix()}")
            val (access, refresh) = issueSession(userId)

            RestAssured.given()
                .header("refreshToken", refresh)
                .`when`()
                .delete("/auth/tokens")
                .then()
                .statusCode(204)

            storedRefresh(userId) shouldBe null
            blacklisted(accessTokenId(access)) shouldBe false
        }
    }
})
