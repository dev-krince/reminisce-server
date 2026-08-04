package com.krince.reminisce.infra.adapter.out

import com.krince.reminisce.infra.security.JwtProvider
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jakarta.servlet.http.HttpServletRequest

@Tags("test", "unitTest")
@DisplayName("JwtProviderAdapter 단위테스트")
class JwtProviderAdapterTest : FunSpec({

    val jwtProvider = mockk<JwtProvider>()
    val adapter = JwtProviderAdapter(jwtProvider)

    val userId = "a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d"
    val role = "ROLE_USER"

    context("generateAccessToken") {
        context("성공") {
            test("jwtProvider.createAccessToken 결과를 반환한다") {
                clearMocks(jwtProvider)
                every { jwtProvider.createAccessToken(id = userId, role = role) } returns "Bearer accessToken"

                val result = adapter.generateAccessToken(userId, role)

                result shouldBe "Bearer accessToken"
                verify(exactly = 1) { jwtProvider.createAccessToken(id = userId, role = role) }
            }
        }
    }

    context("generateRefreshToken") {
        context("성공") {
            test("jwtProvider.createRefreshToken 결과를 반환한다") {
                clearMocks(jwtProvider)
                every { jwtProvider.createRefreshToken(role = role, id = userId) } returns "Bearer refreshToken"

                val result = adapter.generateRefreshToken(userId, role)

                result shouldBe "Bearer refreshToken"
                verify(exactly = 1) { jwtProvider.createRefreshToken(role = role, id = userId) }
            }
        }
    }

    context("extractToken") {
        context("성공") {
            test("jwtProvider.extractToken 결과를 반환한다") {
                clearMocks(jwtProvider)
                every { jwtProvider.extractToken("Bearer xxx") } returns "xxx"

                val result = adapter.extractToken("Bearer xxx")

                result shouldBe "xxx"
                verify(exactly = 1) { jwtProvider.extractToken("Bearer xxx") }
            }
        }
    }

    context("validateRefreshToken") {
        context("성공") {
            test("jwtProvider.validateRefreshToken을 호출한다") {
                clearMocks(jwtProvider)
                every { jwtProvider.validateRefreshToken("rawRefreshToken") } returns Unit

                adapter.validateRefreshToken("rawRefreshToken")

                verify(exactly = 1) { jwtProvider.validateRefreshToken("rawRefreshToken") }
            }
        }
    }

    context("getUserId") {
        context("성공") {
            test("jwtProvider.getId 결과를 반환한다") {
                clearMocks(jwtProvider)
                every { jwtProvider.getId("rawToken") } returns userId

                val result = adapter.getUserId("rawToken")

                result shouldBe userId
                verify(exactly = 1) { jwtProvider.getId("rawToken") }
            }
        }
    }

    context("getRole") {
        context("성공") {
            test("jwtProvider.getRole 결과를 반환한다") {
                clearMocks(jwtProvider)
                every { jwtProvider.getRole("rawToken") } returns role

                val result = adapter.getRole("rawToken")

                result shouldBe role
                verify(exactly = 1) { jwtProvider.getRole("rawToken") }
            }
        }
    }

    context("getTokenId") {
        context("성공") {
            test("jwtProvider.getTokenId 결과를 반환한다") {
                clearMocks(jwtProvider)
                every { jwtProvider.getTokenId("rawToken") } returns "jti-1"

                val result = adapter.getTokenId("rawToken")

                result shouldBe "jti-1"
                verify(exactly = 1) { jwtProvider.getTokenId("rawToken") }
            }
        }
    }

    context("getRemainingExpiration") {
        context("성공") {
            test("jwtProvider.getRemainingExpiration 결과를 반환한다") {
                clearMocks(jwtProvider)
                val remaining = java.time.Duration.ofMinutes(30)
                every { jwtProvider.getRemainingExpiration("rawToken") } returns remaining

                val result = adapter.getRemainingExpiration("rawToken")

                result shouldBe remaining
                verify(exactly = 1) { jwtProvider.getRemainingExpiration("rawToken") }
            }
        }
    }

    context("getUserIdFromRequest") {
        context("성공") {
            test("jwtProvider.getUserIdFromRequest 결과를 반환한다") {
                clearMocks(jwtProvider)
                val request = mockk<HttpServletRequest>()
                every { jwtProvider.getUserIdFromRequest(request) } returns userId

                val result = adapter.getUserIdFromRequest(request)

                result shouldBe userId
                verify(exactly = 1) { jwtProvider.getUserIdFromRequest(request) }
            }
        }
        context("실패") {
            test("jwtProvider가 null을 반환하면 null을 반환한다") {
                clearMocks(jwtProvider)
                val request = mockk<HttpServletRequest>()
                every { jwtProvider.getUserIdFromRequest(request) } returns null

                val result = adapter.getUserIdFromRequest(request)

                result shouldBe null
            }
        }
    }
})
