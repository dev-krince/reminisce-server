package com.krince.reminisce.infra.adapter.`in`.controller

import com.krince.reminisce.infra.adapter.out.persistence.child.entity.ChildOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.childconsent.entity.ChildConsentOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.user.entity.UserOrmEntity
import com.krince.reminisce.shared.response.ExceptionResponseCode
import com.krince.reminisce.shared.response.SuccessResponseCode
import com.krince.reminisce.shared.util.UuidGenerator
import com.krince.reminisce.testutil.TestConfig
import com.krince.reminisce.testutil.fixture.TestAuthUserFixture
import com.krince.reminisce.testutil.fixture.TestChildConsentFixture
import com.krince.reminisce.testutil.fixture.TestChildFixture
import com.krince.reminisce.testutil.fixture.TestJwtTokenFixture
import com.krince.reminisce.testutil.fixture.TestUserFixture
import io.kotest.common.ExperimentalKotest
import io.kotest.core.annotation.DisplayName
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
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
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.test.context.ActiveProfiles
import java.time.Duration
import java.time.LocalDateTime

@OptIn(ExperimentalKotest::class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("localtest")
@Import(TestConfig::class)
@io.kotest.core.annotation.Tags("test", "integrationTest")
@DisplayName("UserControllerImpl 통합테스트")
class UserControllerImplTest(
    @param:LocalServerPort private val port: Int,
    private val testUserFixture: TestUserFixture,
    private val testAuthUserFixture: TestAuthUserFixture,
    private val testChildFixture: TestChildFixture,
    private val testChildConsentFixture: TestChildConsentFixture,
    private val testJwtTokenFixture: TestJwtTokenFixture,
    private val redisTemplate: StringRedisTemplate,
) : FunSpec({

    val refreshTtl = Duration.ofMinutes(30)

    fun kakaoUserEntity(userId: String, providerId: String, email: String? = null): UserOrmEntity =
        UserOrmEntity(
            userId = userId,
            email = email,
            nickname = "카카오",
            provider = "KAKAO",
            role = "ROLE_USER",
            providerId = providerId,
        )

    fun childEntity(guardianId: String): ChildOrmEntity =
        ChildOrmEntity(
            childId = UuidGenerator.generate(),
            guardianId = guardianId,
            nickname = "토토",
            birthYear = 2019,
        )

    fun consentEntity(childId: String): ChildConsentOrmEntity =
        ChildConsentOrmEntity(
            consentId = UuidGenerator.generate(),
            childId = childId,
            consentVersion = "v1.0",
            verificationMethod = "AUTHENTICATED_PARENT",
            consentedAt = LocalDateTime.of(2026, 6, 1, 0, 0),
        )

    fun storedRefresh(userId: String): String? = redisTemplate.opsForValue().get("auth:refresh:$userId")

    fun issueTokens(userId: String): Pair<String, String> {
        val access = testJwtTokenFixture.generateAccessToken(userId)
        val refresh = testJwtTokenFixture.generateRefreshToken(userId)
        redisTemplate.opsForValue().set("auth:refresh:$userId", refresh, refreshTtl)

        return access to refresh
    }

    fun uniqueSuffix(): String = "${System.currentTimeMillis()}-${System.nanoTime()}"

    beforeSpec {
        RestAssured.port = port
        RestAssured.basePath = "/api"
        RestAssured.defaultParser = Parser.JSON
    }

    beforeTest {
        testChildConsentFixture.deleteAllBatch()
        testChildFixture.deleteAllBatch()
        testUserFixture.deleteAllBatch()
        redisTemplate.connectionFactory?.connection?.serverCommands()?.flushAll()
    }

    context("getUser") {
        context("성공") {
            test("유효한 토큰으로 /me를 조회하면 200과 본인 정보를 반환한다") {
                val userId = "user-${uniqueSuffix()}"
                val email = "user${uniqueSuffix()}@example.com"
                val savedUser = testUserFixture.saveUser(kakaoUserEntity(userId, "kakao-${uniqueSuffix()}", email))
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
                    .body("data.nickname", equalTo("카카오"))
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
                testUserFixture.saveUser(kakaoUserEntity(userId, "kakao-${uniqueSuffix()}"))

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
                val savedUser = testUserFixture.saveUser(kakaoUserEntity(userId, "kakao-${uniqueSuffix()}"))
                val token = testJwtTokenFixture.generateAccessToken(savedUser.userId, savedUser.role)
                val otherUserId = "other-${uniqueSuffix()}"
                val otherEmail = "other${uniqueSuffix()}@example.com"
                testUserFixture.saveUser(kakaoUserEntity(otherUserId, "kakao-${uniqueSuffix()}", otherEmail))

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

    context("withdraw") {
        context("성공") {
            test("아이2·동의2를 가진 보호자가 탈퇴하면 204이고 본인·아이·동의가 사라지며 타 보호자 데이터는 남는다") {
                val guardianId = testAuthUserFixture.saveKakaoUser("kakao-${uniqueSuffix()}")
                val firstChild = testChildFixture.saveChild(childEntity(guardianId))
                val secondChild = testChildFixture.saveChild(childEntity(guardianId))
                testChildConsentFixture.saveConsent(consentEntity(firstChild.childId))
                testChildConsentFixture.saveConsent(consentEntity(secondChild.childId))

                val otherGuardianId = testAuthUserFixture.saveKakaoUser("kakao-${uniqueSuffix()}")
                val otherChild = testChildFixture.saveChild(childEntity(otherGuardianId))
                testChildConsentFixture.saveConsent(consentEntity(otherChild.childId))

                val (access, _) = issueTokens(guardianId)
                storedRefresh(guardianId).shouldNotBeNull()

                RestAssured.given()
                    .header("Authorization", access)
                    .`when`()
                    .delete("/users/me")
                    .then()
                    .statusCode(204)

                testUserFixture.existsById(guardianId) shouldBe false
                testChildFixture.countByGuardianId(guardianId) shouldBe 0L
                testChildConsentFixture.findAllByChildId(firstChild.childId).size shouldBe 0
                testChildConsentFixture.findAllByChildId(secondChild.childId).size shouldBe 0

                testUserFixture.findById(otherGuardianId).shouldNotBeNull()
                testChildFixture.countByGuardianId(otherGuardianId) shouldBe 1L
                testChildConsentFixture.findAllByChildId(otherChild.childId).size shouldBe 1
            }

            test("탈퇴에 쓴 액세스 토큰으로 인증 API 재요청하면 401 LOGGED_OUT_TOKEN이다") {
                val guardianId = testAuthUserFixture.saveKakaoUser("kakao-${uniqueSuffix()}")
                val (access, _) = issueTokens(guardianId)

                RestAssured.given()
                    .header("Authorization", access)
                    .`when`()
                    .delete("/users/me")
                    .then()
                    .statusCode(204)

                RestAssured.given()
                    .header("Authorization", access)
                    .`when`()
                    .get("/users/me")
                    .then()
                    .statusCode(401)
                    .body("detailCode", equalTo(ExceptionResponseCode.LOGGED_OUT_TOKEN.detailCode))
            }

            test("탈퇴한 유저의 비블랙리스트 액세스 토큰으로 인증 API 요청하면 500이 아니라 401 INVALID_TOKEN이다") {
                val guardianId = testAuthUserFixture.saveKakaoUser("kakao-${uniqueSuffix()}")
                val (access, _) = issueTokens(guardianId)
                val separateAccess = testJwtTokenFixture.generateAccessToken(guardianId)

                RestAssured.given()
                    .header("Authorization", access)
                    .`when`()
                    .delete("/users/me")
                    .then()
                    .statusCode(204)

                RestAssured.given()
                    .header("Authorization", separateAccess)
                    .`when`()
                    .get("/users/me")
                    .then()
                    .statusCode(401)
                    .body("detailCode", equalTo(ExceptionResponseCode.INVALID_TOKEN.detailCode))
            }

            test("탈퇴하면 Redis 저장 리프레시가 삭제되어 그 리프레시로 재발급하면 거부한다") {
                val guardianId = testAuthUserFixture.saveKakaoUser("kakao-${uniqueSuffix()}")
                val (access, refresh) = issueTokens(guardianId)

                RestAssured.given()
                    .header("Authorization", access)
                    .`when`()
                    .delete("/users/me")
                    .then()
                    .statusCode(204)

                storedRefresh(guardianId) shouldBe null

                RestAssured.given()
                    .header("refreshToken", refresh)
                    .`when`()
                    .post("/auth/tokens/refresh")
                    .then()
                    .statusCode(402)
                    .body("detailCode", equalTo(ExceptionResponseCode.INVALID_REFRESH_TOKEN.detailCode))
            }
        }
        context("예외케이스") {
            test("토큰 없이 탈퇴를 요청하면 401 EMPTY_TOKEN이다") {
                RestAssured.given()
                    .contentType(ContentType.JSON)
                    .`when`()
                    .delete("/users/me")
                    .then()
                    .statusCode(401)
                    .body("detailCode", equalTo(ExceptionResponseCode.EMPTY_TOKEN.detailCode))
            }
        }
    }
})
