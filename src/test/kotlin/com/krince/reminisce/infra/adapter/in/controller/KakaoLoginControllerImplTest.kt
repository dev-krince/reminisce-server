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
@DisplayName("카카오 로그인 통합테스트")
class KakaoLoginControllerImplTest(
    @param:LocalServerPort private val port: Int,
    private val testUserFixture: TestUserFixture,
    private val userRepository: UserRepository,
) : FunSpec({

    fun jsonResponse(body: String): MockResponse =
        MockResponse()
            .setResponseCode(200)
            .addHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            .setBody(body)

    fun kakaoUserBody(kakaoId: Long): String =
        """{"id":$kakaoId,"kakao_account":{"email":"k@example.com","profile":{"nickname":"카카오회원"}}}"""

    fun postKakaoLogin(authorizationCode: String?) =
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(mapOf("authorizationCode" to authorizationCode))
            .`when`()
            .post("/auth/tokens/kakao")
            .then()

    fun postKakaoLoginWithoutBody() =
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body("{}")
            .`when`()
            .post("/auth/tokens/kakao")
            .then()

    beforeSpec {
        RestAssured.port = port
        RestAssured.basePath = "/api"
        RestAssured.defaultParser = Parser.JSON
    }

    beforeTest {
        testUserFixture.deleteAllBatch()
    }

    context("카카오 로그인") {
        test("유효한 인가코드로 로그인하면 200과 Authorization·refreshToken 헤더를 반환하고 유저가 KAKAO provider로 저장된다") {
            val kakaoId = 700100L
            mockServer.enqueue(jsonResponse("""{"access_token":"kakao-access-token"}"""))
            mockServer.enqueue(jsonResponse(kakaoUserBody(kakaoId)))

            val response = postKakaoLogin("valid-code")
                .statusCode(200)
                .extract()

            val accessHeader = response.header("Authorization")
            val refreshHeader = response.header("refreshToken")
            accessHeader.shouldNotBeNull()
            accessHeader shouldStartWith "Bearer "
            refreshHeader.shouldNotBeNull()
            refreshHeader shouldStartWith "Bearer "

            val savedUser = userRepository.findByProviderAndProviderId("KAKAO", kakaoId.toString())
            savedUser.shouldNotBeNull()
            savedUser.provider shouldBe "KAKAO"
            savedUser.providerId shouldBe kakaoId.toString()
            savedUser.email shouldBe "k@example.com"
        }
        test("이미 존재하는 카카오 계정이면 재생성 없이 토큰을 반환한다") {
            val kakaoId = 700200L
            mockServer.enqueue(jsonResponse("""{"access_token":"kakao-access-token"}"""))
            mockServer.enqueue(jsonResponse(kakaoUserBody(kakaoId)))
            postKakaoLogin("valid-code").statusCode(200)

            mockServer.enqueue(jsonResponse("""{"access_token":"kakao-access-token"}"""))
            mockServer.enqueue(jsonResponse(kakaoUserBody(kakaoId)))
            postKakaoLogin("valid-code").statusCode(200)

            val count = userRepository.findAll().count { it.providerId == kakaoId.toString() }
            count shouldBe 1
        }
        test("인가코드가 blank이면 400과 INVALID_DTO_PARAMETER를 반환한다") {
            postKakaoLogin("")
                .statusCode(400)
                .body("detailCode", equalTo(ExceptionResponseCode.INVALID_DTO_PARAMETER.detailCode))
        }
        test("authorizationCode 필드가 없으면 400과 INVALID_DTO_PARAMETER를 반환한다") {
            postKakaoLoginWithoutBody()
                .statusCode(400)
                .body("detailCode", equalTo(ExceptionResponseCode.INVALID_DTO_PARAMETER.detailCode))
        }
        test("카카오 토큰 교환 실패 시 502와 SOCIAL_AUTH_FAILED를 반환한다") {
            mockServer.enqueue(MockResponse().setResponseCode(401))

            postKakaoLogin("bad-code")
                .statusCode(502)
                .body("detailCode", equalTo(ExceptionResponseCode.SOCIAL_AUTH_FAILED.detailCode))
        }
        test("카카오 사용자 정보 조회 실패 시 502와 SOCIAL_AUTH_FAILED를 반환한다") {
            mockServer.enqueue(jsonResponse("""{"access_token":"kakao-access-token"}"""))
            mockServer.enqueue(MockResponse().setResponseCode(500))

            postKakaoLogin("valid-code")
                .statusCode(502)
                .body("detailCode", equalTo(ExceptionResponseCode.SOCIAL_AUTH_FAILED.detailCode))
        }
    }
}) {
    companion object {
        val mockServer: MockWebServer = MockWebServer().also { it.start() }

        @JvmStatic
        @DynamicPropertySource
        fun overrideKakaoOAuthProperties(registry: DynamicPropertyRegistry) {
            registry.add("oauth.kakao.token-uri") { mockServer.url("/kakao/oauth/token").toString() }
            registry.add("oauth.kakao.user-info-uri") { mockServer.url("/kakao/userinfo").toString() }
        }
    }
}
