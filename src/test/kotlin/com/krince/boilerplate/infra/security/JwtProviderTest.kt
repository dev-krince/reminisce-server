package com.krince.boilerplate.infra.security

import com.krince.boilerplate.shared.exception.UnauthorizedRefreshTokenException
import com.krince.boilerplate.shared.response.ExceptionResponseCode.*
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import io.mockk.every
import io.mockk.mockk
import jakarta.servlet.http.HttpServletRequest
import java.util.Base64

@Tags("test", "unitTest")
@DisplayName("JwtProvider 단위테스트")
class JwtProviderTest : FunSpec({

    val secretKeyBase64 = Base64.getEncoder().encodeToString("01234567890123456789012345678901".toByteArray())
    val accessExpired = 86400000L
    val refreshExpired = 1209600000L
    val provider = JwtProvider(secretKeyBase64, accessExpired, refreshExpired)

    val uuid1 = "a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d"
    val uuid2 = "b2c3d4e5-f6a7-5b6c-9d0e-1f2a3b4c5d6e"
    val uuid99 = "c3d4e5f6-a7b8-6c7d-0e1f-2a3b4c5d6e7f"
    val uuid42 = "d4e5f6a7-b8c9-7d8e-1f2a-3b4c5d6e7f8a"

    fun String.rawToken(): String = removePrefix("Bearer ").trim()

    context("createAccessToken") {
        context("성공") {
            test("id와 role이 포함된 Bearer 토큰을 반환한다") {
                val token = provider.createAccessToken(uuid1, "ROLE_USER")
                token.shouldStartWith("Bearer ")
                provider.getId(token.rawToken()) shouldBe uuid1
                provider.getRole(token.rawToken()) shouldBe "ROLE_USER"
            }
        }
    }

    context("createRefreshToken") {
        context("성공") {
            test("id와 role이 포함된 Bearer 리프레시 토큰을 반환한다") {
                val token = provider.createRefreshToken(uuid2, "ROLE_ADMIN")
                token.shouldStartWith("Bearer ")
                provider.getId(token.rawToken()) shouldBe uuid2
                provider.getRole(token.rawToken()) shouldBe "ROLE_ADMIN"
            }
        }
    }

    context("isValidToken") {
        context("성공") {
            test("유효한 토큰이면 true를 반환한다") {
                val token = provider.createAccessToken(uuid1, "ROLE_USER").rawToken()
                provider.isValidToken(token) shouldBe true
            }
        }
        context("실패") {
            test("잘못된 토큰이면 false를 반환한다") {
                provider.isValidToken("invalid.jwt.token") shouldBe false
            }
        }
    }

    context("validateRefreshToken") {
        context("성공") {
            test("유효한 리프레시 토큰이면 예외를 던지지 않는다") {
                val token = provider.createRefreshToken(uuid1, "ROLE_USER").rawToken()
                provider.validateRefreshToken(token)
            }
        }
        context("실패") {
            test("액세스 토큰을 넘기면 UNAUTHORIZED_REFRESH_TOKEN 예외를 던진다") {
                val accessToken = provider.createAccessToken(uuid1, "ROLE_USER").rawToken()
                val ex = shouldThrow<UnauthorizedRefreshTokenException> { provider.validateRefreshToken(accessToken) }
                ex.exceptionResponseCode shouldBe UNAUTHORIZED_REFRESH_TOKEN
            }
            test("잘못된 토큰이면 INVALID_REFRESH_TOKEN 예외를 던진다") {
                val ex = shouldThrow<UnauthorizedRefreshTokenException> { provider.validateRefreshToken("bad-token") }
                ex.exceptionResponseCode shouldBe INVALID_REFRESH_TOKEN
            }
        }
    }

    context("getId") {
        context("성공") {
            test("토큰의 subject를 반환한다") {
                val token = provider.createAccessToken(uuid99, "ROLE_USER").rawToken()
                provider.getId(token) shouldBe uuid99
            }
        }
    }

    context("getRole") {
        context("성공") {
            test("토큰의 role claim을 반환한다") {
                val token = provider.createAccessToken(uuid1, "ROLE_ADMIN").rawToken()
                provider.getRole(token) shouldBe "ROLE_ADMIN"
            }
        }
    }

    context("getAccessTokenFromRequest") {
        context("성공") {
            test("Authorization 헤더 값이 있으면 그 값을 반환한다") {
                val request = mockk<HttpServletRequest>()
                every { request.getHeader("Authorization") } returns "Bearer abc123"
                provider.getAccessTokenFromRequest(request) shouldBe "Bearer abc123"
            }
        }
        context("실패") {
            test("헤더가 없거나 비어있으면 null을 반환한다") {
                val requestNull = mockk<HttpServletRequest>()
                every { requestNull.getHeader("Authorization") } returns null
                provider.getAccessTokenFromRequest(requestNull) shouldBe null
                val requestBlank = mockk<HttpServletRequest>()
                every { requestBlank.getHeader("Authorization") } returns "   "
                provider.getAccessTokenFromRequest(requestBlank) shouldBe null
            }
        }
    }

    context("getUserIdFromRequest") {
        context("성공") {
            test("Authorization 헤더에 유효한 토큰이 있으면 subject를 반환한다") {
                val rawToken = provider.createAccessToken(uuid42, "ROLE_USER").rawToken()
                val request = mockk<HttpServletRequest>()
                every { request.getHeader("Authorization") } returns rawToken
                provider.getUserIdFromRequest(request) shouldBe uuid42
            }
        }
        context("실패") {
            test("헤더가 없으면 null을 반환한다") {
                val request = mockk<HttpServletRequest>()
                every { request.getHeader("Authorization") } returns null
                provider.getUserIdFromRequest(request) shouldBe null
            }
        }
    }

    context("extractToken") {
        context("성공") {
            test("Bearer 접두어를 제거한 토큰을 반환한다") {
                val raw = "eyJhbGciOiJIUzI1NiJ9.xxx"
                provider.extractToken("Bearer $raw") shouldBe raw
            }
        }
        context("실패") {
            test("Bearer로 시작하지 않으면 IllegalArgumentException을 던진다") {
                shouldThrow<IllegalArgumentException> { provider.extractToken("InvalidPrefix token") }
            }
        }
    }

    context("getTokenType") {
        context("성공") {
            test("액세스 토큰이면 accessToken을 반환한다") {
                val token = provider.createAccessToken(uuid1, "ROLE_USER").rawToken()
                provider.getTokenType(token) shouldBe "accessToken"
            }
            test("리프레시 토큰이면 refreshToken을 반환한다") {
                val token = provider.createRefreshToken(uuid1, "ROLE_USER").rawToken()
                provider.getTokenType(token) shouldBe "refreshToken"
            }
        }
    }
})
