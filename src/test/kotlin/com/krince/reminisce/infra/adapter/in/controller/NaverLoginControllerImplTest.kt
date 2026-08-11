package com.krince.reminisce.infra.adapter.`in`.controller

import com.krince.reminisce.infra.adapter.out.persistence.user.UserRepository
import com.krince.reminisce.shared.response.ExceptionResponseCode
import com.krince.reminisce.testutil.TestConfig
import com.krince.reminisce.testutil.fixture.TestUserFixture
import io.kotest.core.annotation.DisplayName
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.parsing.Parser
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.hamcrest.Matchers.equalTo
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("localtest")
@Import(TestConfig::class)
@io.kotest.core.annotation.Tags("test", "integrationTest")
@DisplayName("네이버 로그인 통합테스트")
class NaverLoginControllerImplTest(
    @param:LocalServerPort private val port: Int,
    private val testUserFixture: TestUserFixture,
    private val userRepository: UserRepository,
) : FunSpec({

    fun jsonResponse(body: String): MockResponse =
        MockResponse()
            .setResponseCode(200)
            .addHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            .setBody(body)

    fun userInfoBody(naverId: String, email: String): String =
        """{"resultcode":"00","message":"success","response":{"id":"$naverId","email":"$email","nickname":"네이버회원"}}"""

    fun postNaverLogin(authorizationCode: String?, state: String?) =
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(mapOf("authorizationCode" to authorizationCode, "state" to state))
            .`when`()
            .post("/auth/tokens/naver")
            .then()

    fun postNaverLoginWithoutBody() =
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body("{}")
            .`when`()
            .post("/auth/tokens/naver")
            .then()

    beforeSpec {
        RestAssured.port = port
        RestAssured.basePath = "/api"
        RestAssured.defaultParser = Parser.JSON
    }

    beforeTest {
        testUserFixture.deleteAllBatch()
    }

    context("네이버 로그인") {
        test("유효한 인가코드+state로 로그인하면 200과 Authorization·refreshToken 헤더를 반환하고 유저가 NAVER provider로 저장된다") {
            val naverId = "naver-id-integration-1"
            mockServer.enqueue(jsonResponse("""{"access_token":"naver-access-token"}"""))
            mockServer.enqueue(jsonResponse(userInfoBody(naverId, "n@example.com")))

            val response = postNaverLogin("valid-code", "valid-state")
                .statusCode(200)
                .extract()

            val accessHeader = response.header("Authorization")
            val refreshHeader = response.header("refreshToken")
            accessHeader.shouldNotBeNull()
            accessHeader shouldStartWith "Bearer "
            refreshHeader.shouldNotBeNull()
            refreshHeader shouldStartWith "Bearer "

            val savedUser = userRepository.findByProviderAndProviderId("NAVER", naverId)
            savedUser.shouldNotBeNull()
            savedUser.provider shouldBe "NAVER"
            savedUser.providerId shouldBe naverId
            savedUser.email shouldBe "n@example.com"
        }
        test("이미 존재하는 네이버 계정이면 재생성 없이 토큰을 반환한다") {
            val naverId = "naver-id-integration-existing"
            mockServer.enqueue(jsonResponse("""{"access_token":"naver-access-token"}"""))
            mockServer.enqueue(jsonResponse(userInfoBody(naverId, "n2@example.com")))
            postNaverLogin("valid-code", "valid-state").statusCode(200)

            mockServer.enqueue(jsonResponse("""{"access_token":"naver-access-token"}"""))
            mockServer.enqueue(jsonResponse(userInfoBody(naverId, "n2@example.com")))
            postNaverLogin("valid-code", "valid-state").statusCode(200)

            val count = userRepository.findAll().count { it.providerId == naverId }
            count shouldBe 1
        }
        test("인가코드가 blank이면 400과 INVALID_DTO_PARAMETER를 반환한다") {
            postNaverLogin("", "valid-state")
                .statusCode(400)
                .body("detailCode", equalTo(ExceptionResponseCode.INVALID_DTO_PARAMETER.detailCode))
        }
        test("authorizationCode 필드가 없으면 400과 INVALID_DTO_PARAMETER를 반환한다") {
            postNaverLoginWithoutBody()
                .statusCode(400)
                .body("detailCode", equalTo(ExceptionResponseCode.INVALID_DTO_PARAMETER.detailCode))
        }
        test("네이버 토큰 교환 실패 시 502와 SOCIAL_AUTH_FAILED를 반환한다") {
            mockServer.enqueue(MockResponse().setResponseCode(401))

            postNaverLogin("bad-code", "valid-state")
                .statusCode(502)
                .body("detailCode", equalTo(ExceptionResponseCode.SOCIAL_AUTH_FAILED.detailCode))
        }
        test("네이버 userinfo 조회 실패 시 502와 SOCIAL_AUTH_FAILED를 반환한다") {
            mockServer.enqueue(jsonResponse("""{"access_token":"naver-access-token"}"""))
            mockServer.enqueue(MockResponse().setResponseCode(500))

            postNaverLogin("valid-code", "valid-state")
                .statusCode(502)
                .body("detailCode", equalTo(ExceptionResponseCode.SOCIAL_AUTH_FAILED.detailCode))
        }
    }
}) {
    companion object {
        val mockServer: MockWebServer = MockWebServer().also { it.start() }

        @JvmStatic
        @DynamicPropertySource
        fun overrideNaverOAuthProperties(registry: DynamicPropertyRegistry) {
            registry.add("oauth.naver.token-uri") { mockServer.url("/naver/oauth/token").toString() }
            registry.add("oauth.naver.user-info-uri") { mockServer.url("/naver/userinfo").toString() }
        }
    }
}
