package com.krince.reminisce.infra.adapter.out.auth

import com.krince.reminisce.infra.config.properties.NaverOAuthProperties
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
@DisplayName("NaverOAuthAdapter 단위테스트")
class NaverOAuthAdapterTest : FunSpec({

    lateinit var server: MockWebServer
    lateinit var adapter: NaverOAuthAdapter

    beforeEach {
        server = MockWebServer()
        server.start()
        val properties = NaverOAuthProperties(
            clientId = "client-id",
            clientSecret = "client-secret",
            redirectUri = "http://localhost/callback",
            tokenUri = server.url("/oauth/token").toString(),
            userInfoUri = server.url("/userinfo").toString(),
        )
        adapter = NaverOAuthAdapter(properties, RestClient.builder())
    }

    afterEach { server.shutdown() }

    fun jsonResponse(body: String): MockResponse =
        MockResponse()
            .setResponseCode(200)
            .addHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            .setBody(body)

    context("exchangeCodeForUser") {
        context("성공") {
            test("토큰 교환 후 userinfo의 중첩 response에서 사용자 정보를 매핑한다") {
                server.enqueue(jsonResponse("""{"access_token":"naver-access"}"""))
                server.enqueue(
                    jsonResponse(
                        """{"resultcode":"00","message":"success","response":{"id":"naver-id-1","email":"n@example.com","nickname":"네이버유저"}}""",
                    ),
                )

                val result = adapter.exchangeCodeForUser("auth-code", "state")

                result.id shouldBe "naver-id-1"
                result.email shouldBe "n@example.com"
                result.nickname shouldBe "네이버유저"
            }
            test("토큰 교환 form에 state가 포함된다") {
                server.enqueue(jsonResponse("""{"access_token":"naver-access"}"""))
                server.enqueue(
                    jsonResponse(
                        """{"resultcode":"00","message":"success","response":{"id":"naver-id-state","nickname":"닉"}}""",
                    ),
                )

                adapter.exchangeCodeForUser("auth-code", "state-value")

                val tokenRequest = server.takeRequest()
                tokenRequest.body.readUtf8().contains("state=state-value") shouldBe true
            }
            test("네이버가 이메일을 주지 않으면 email은 null이다") {
                server.enqueue(jsonResponse("""{"access_token":"naver-access"}"""))
                server.enqueue(
                    jsonResponse(
                        """{"resultcode":"00","message":"success","response":{"id":"naver-id-2","nickname":"닉"}}""",
                    ),
                )

                val result = adapter.exchangeCodeForUser("auth-code", "state")

                result.email shouldBe null
                result.nickname shouldBe "닉"
            }
            test("nickname이 없으면 name으로 폴백한다") {
                server.enqueue(jsonResponse("""{"access_token":"naver-access"}"""))
                server.enqueue(
                    jsonResponse(
                        """{"resultcode":"00","message":"success","response":{"id":"naver-id-3","name":"홍길동"}}""",
                    ),
                )

                val result = adapter.exchangeCodeForUser("auth-code", "state")

                result.nickname shouldBe "홍길동"
            }
            test("nickname·name이 모두 없으면 기본 닉네임 '네이버회원'이 적용된다") {
                server.enqueue(jsonResponse("""{"access_token":"naver-access"}"""))
                server.enqueue(
                    jsonResponse(
                        """{"resultcode":"00","message":"success","response":{"id":"naver-id-4"}}""",
                    ),
                )

                val result = adapter.exchangeCodeForUser("auth-code", "state")

                result.nickname shouldBe "네이버회원"
            }
        }
        context("실패") {
            test("토큰 교환이 실패하면 SOCIAL_AUTH_FAILED로 던진다") {
                server.enqueue(MockResponse().setResponseCode(401))

                val exception = shouldThrow<SocialAuthException> {
                    adapter.exchangeCodeForUser("bad-code", "state")
                }

                exception.exceptionResponseCode shouldBe ExceptionResponseCode.SOCIAL_AUTH_FAILED
            }
            test("userinfo 호출이 실패하면 SOCIAL_AUTH_FAILED로 던진다") {
                server.enqueue(jsonResponse("""{"access_token":"naver-access"}"""))
                server.enqueue(MockResponse().setResponseCode(500))

                val exception = shouldThrow<SocialAuthException> {
                    adapter.exchangeCodeForUser("auth-code", "state")
                }

                exception.exceptionResponseCode shouldBe ExceptionResponseCode.SOCIAL_AUTH_FAILED
            }
            test("response 래퍼가 없으면 SOCIAL_AUTH_FAILED로 던진다") {
                server.enqueue(jsonResponse("""{"access_token":"naver-access"}"""))
                server.enqueue(jsonResponse("""{"resultcode":"024","message":"unauthorized"}"""))

                val exception = shouldThrow<SocialAuthException> {
                    adapter.exchangeCodeForUser("auth-code", "state")
                }

                exception.exceptionResponseCode shouldBe ExceptionResponseCode.SOCIAL_AUTH_FAILED
            }
        }
    }
})
