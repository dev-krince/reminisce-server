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
@DisplayName("구글 로그인 통합테스트")
class GoogleLoginControllerImplTest(
    @param:LocalServerPort private val port: Int,
    private val testUserFixture: TestUserFixture,
    private val userRepository: UserRepository,
) : FunSpec({

    fun jsonResponse(body: String): MockResponse =
        MockResponse()
            .setResponseCode(200)
            .addHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            .setBody(body)

    fun postGoogleLogin(authorizationCode: String?) =
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(mapOf("authorizationCode" to authorizationCode))
            .`when`()
            .post("/auth/tokens/google")
            .then()

    fun postGoogleLoginWithoutBody() =
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body("{}")
            .`when`()
            .post("/auth/tokens/google")
            .then()

    beforeSpec {
        RestAssured.port = port
        RestAssured.basePath = "/api"
        RestAssured.defaultParser = Parser.JSON
    }

    beforeTest {
        testUserFixture.deleteAllBatch()
    }

    context("구글 로그인") {
        test("유효한 인가코드로 로그인하면 200과 Authorization·refreshToken 헤더를 반환하고 유저가 GOOGLE provider로 저장된다") {
            val googleSub = "google-sub-integration-1"
            mockServer.enqueue(jsonResponse("""{"access_token":"google-access-token"}"""))
            mockServer.enqueue(jsonResponse("""{"sub":"$googleSub","email":"g@example.com","name":"구글회원"}"""))

            val response = postGoogleLogin("valid-code")
                .statusCode(200)
                .extract()

            val accessHeader = response.header("Authorization")
            val refreshHeader = response.header("refreshToken")
            accessHeader.shouldNotBeNull()
            accessHeader shouldStartWith "Bearer "
            refreshHeader.shouldNotBeNull()
            refreshHeader shouldStartWith "Bearer "

            val savedUser = userRepository.findByProviderAndProviderId("GOOGLE", googleSub)
            savedUser.shouldNotBeNull()
            savedUser.provider shouldBe "GOOGLE"
            savedUser.providerId shouldBe googleSub
            savedUser.email shouldBe "g@example.com"
        }
        test("이미 존재하는 구글 계정이면 재생성 없이 토큰을 반환한다") {
            val googleSub = "google-sub-integration-existing"
            mockServer.enqueue(jsonResponse("""{"access_token":"google-access-token"}"""))
            mockServer.enqueue(jsonResponse("""{"sub":"$googleSub","email":"g2@example.com","name":"구글회원"}"""))
            postGoogleLogin("valid-code").statusCode(200)

            mockServer.enqueue(jsonResponse("""{"access_token":"google-access-token"}"""))
            mockServer.enqueue(jsonResponse("""{"sub":"$googleSub","email":"g2@example.com","name":"구글회원"}"""))
            postGoogleLogin("valid-code").statusCode(200)

            val count = userRepository.findAll().count { it.providerId == googleSub }
            count shouldBe 1
        }
        test("인가코드가 blank이면 400과 INVALID_DTO_PARAMETER를 반환한다") {
            postGoogleLogin("")
                .statusCode(400)
                .body("detailCode", equalTo(ExceptionResponseCode.INVALID_DTO_PARAMETER.detailCode))
        }
        test("authorizationCode 필드가 없으면 400과 INVALID_DTO_PARAMETER를 반환한다") {
            postGoogleLoginWithoutBody()
                .statusCode(400)
                .body("detailCode", equalTo(ExceptionResponseCode.INVALID_DTO_PARAMETER.detailCode))
        }
        test("구글 토큰 교환 실패 시 502와 SOCIAL_AUTH_FAILED를 반환한다") {
            mockServer.enqueue(MockResponse().setResponseCode(401))

            postGoogleLogin("bad-code")
                .statusCode(502)
                .body("detailCode", equalTo(ExceptionResponseCode.SOCIAL_AUTH_FAILED.detailCode))
        }
        test("구글 userinfo 조회 실패 시 502와 SOCIAL_AUTH_FAILED를 반환한다") {
            mockServer.enqueue(jsonResponse("""{"access_token":"google-access-token"}"""))
            mockServer.enqueue(MockResponse().setResponseCode(500))

            postGoogleLogin("valid-code")
                .statusCode(502)
                .body("detailCode", equalTo(ExceptionResponseCode.SOCIAL_AUTH_FAILED.detailCode))
        }
    }
}) {
    companion object {
        val mockServer: MockWebServer = MockWebServer().also { it.start() }

        @JvmStatic
        @DynamicPropertySource
        fun overrideGoogleOAuthProperties(registry: DynamicPropertyRegistry) {
            registry.add("oauth.google.token-uri") { mockServer.url("/google/oauth/token").toString() }
            registry.add("oauth.google.user-info-uri") { mockServer.url("/google/userinfo").toString() }
        }
    }
}
