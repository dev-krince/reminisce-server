package com.krince.reminisce.infra.adapter.out.auth

import com.krince.reminisce.infra.config.properties.GoogleOAuthProperties
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
@DisplayName("GoogleOAuthAdapter 단위테스트")
class GoogleOAuthAdapterTest : FunSpec({

    lateinit var server: MockWebServer
    lateinit var adapter: GoogleOAuthAdapter

    beforeEach {
        server = MockWebServer()
        server.start()
        val properties = GoogleOAuthProperties(
            clientId = "client-id",
            clientSecret = "client-secret",
            redirectUri = "http://localhost/callback",
            tokenUri = server.url("/oauth/token").toString(),
            userInfoUri = server.url("/userinfo").toString(),
        )
        adapter = GoogleOAuthAdapter(properties, RestClient.builder())
    }

    afterEach { server.shutdown() }

    fun jsonResponse(body: String): MockResponse =
        MockResponse()
            .setResponseCode(200)
            .addHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            .setBody(body)

    context("exchangeCodeForUser") {
        context("성공") {
            test("토큰 교환 후 userinfo로 구글 사용자 정보를 받아 매핑한다") {
                server.enqueue(jsonResponse("""{"access_token":"google-access"}"""))
                server.enqueue(jsonResponse("""{"sub":"google-sub-1","email":"g@example.com","name":"구글회원"}"""))

                val result = adapter.exchangeCodeForUser("auth-code")

                result.id shouldBe "google-sub-1"
                result.email shouldBe "g@example.com"
                result.nickname shouldBe "구글회원"
            }
            test("구글이 이메일을 주지 않으면 email은 null이다") {
                server.enqueue(jsonResponse("""{"access_token":"google-access"}"""))
                server.enqueue(jsonResponse("""{"sub":"google-sub-2","name":"닉"}"""))

                val result = adapter.exchangeCodeForUser("auth-code")

                result.email shouldBe null
                result.nickname shouldBe "닉"
            }
            test("name이 blank이면 기본 닉네임 '구글회원'이 적용된다") {
                server.enqueue(jsonResponse("""{"access_token":"google-access"}"""))
                server.enqueue(jsonResponse("""{"sub":"google-sub-3","name":""}"""))

                val result = adapter.exchangeCodeForUser("auth-code")

                result.nickname shouldBe "구글회원"
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
            test("userinfo 호출이 실패하면 SOCIAL_AUTH_FAILED로 던진다") {
                server.enqueue(jsonResponse("""{"access_token":"google-access"}"""))
                server.enqueue(MockResponse().setResponseCode(500))

                val exception = shouldThrow<SocialAuthException> {
                    adapter.exchangeCodeForUser("auth-code")
                }

                exception.exceptionResponseCode shouldBe ExceptionResponseCode.SOCIAL_AUTH_FAILED
            }
        }
    }
})
