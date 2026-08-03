package com.krince.reminisce.infra.adapter.out.auth

import com.krince.reminisce.infra.config.properties.KakaoOAuthProperties
import com.krince.reminisce.shared.exception.SocialAuthException
import com.krince.reminisce.shared.response.ExceptionResponseCode
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.springframework.http.HttpHeaders
import org.springframework.web.client.RestClient

@Tags("test", "unitTest")
@DisplayName("KakaoOAuthAdapter 단위테스트")
class KakaoOAuthAdapterTest : FunSpec({

    lateinit var server: MockWebServer
    lateinit var adapter: KakaoOAuthAdapter

    beforeEach {
        server = MockWebServer()
        server.start()
        val properties = KakaoOAuthProperties(
            clientId = "client-id",
            clientSecret = "client-secret",
            redirectUri = "http://localhost/callback",
            tokenUri = server.url("/oauth/token").toString(),
            userInfoUri = server.url("/v2/user/me").toString(),
        )
        adapter = KakaoOAuthAdapter(properties, RestClient.builder())
    }

    afterEach { server.shutdown() }

    fun jsonResponse(body: String): MockResponse =
        MockResponse()
            .setResponseCode(200)
            .addHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            .setBody(body)

    context("exchangeCodeForUser") {
        context("성공") {
            test("토큰 교환 후 user/me로 카카오 사용자 정보를 받아 매핑한다") {
                server.enqueue(jsonResponse("""{"access_token":"kakao-access"}"""))
                server.enqueue(jsonResponse("""{"id":9999,"kakao_account":{"email":"k@example.com","profile":{"nickname":"카카오회원"}}}"""))

                val result = adapter.exchangeCodeForUser("auth-code")

                result.id shouldBe "9999"
                result.email shouldBe "k@example.com"
                result.nickname shouldBe "카카오회원"
            }
            test("카카오가 이메일을 주지 않으면 email은 null이다") {
                server.enqueue(jsonResponse("""{"access_token":"kakao-access"}"""))
                server.enqueue(jsonResponse("""{"id":1,"kakao_account":{"profile":{"nickname":"닉"}}}"""))

                val result = adapter.exchangeCodeForUser("auth-code")

                result.email shouldBe null
                result.nickname shouldBe "닉"
            }
        }
        context("실패") {
            test("토큰 교환이 실패하면 SOCIAL_AUTH_FAILED로 던진다") {
                server.enqueue(MockResponse().setResponseCode(401))

                val exception = shouldThrow<SocialAuthException> {
                    adapter.exchangeCodeForUser("bad-code")
                }

                exception.exceptionResponseCode shouldBe ExceptionResponseCode.SOCIAL_AUTH_FAILED
            }
            test("user/me 호출이 실패하면 SOCIAL_AUTH_FAILED로 던진다") {
                server.enqueue(jsonResponse("""{"access_token":"kakao-access"}"""))
                server.enqueue(MockResponse().setResponseCode(500))

                val exception = shouldThrow<SocialAuthException> {
                    adapter.exchangeCodeForUser("auth-code")
                }

                exception.exceptionResponseCode shouldBe ExceptionResponseCode.SOCIAL_AUTH_FAILED
            }
        }
    }
})
