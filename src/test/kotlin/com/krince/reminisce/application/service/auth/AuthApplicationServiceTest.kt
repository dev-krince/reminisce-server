package com.krince.reminisce.application.service.auth

import com.krince.reminisce.application.port.`in`.auth.command.KakaoLoginCommand
import com.krince.reminisce.application.port.`in`.auth.command.LoginCommand
import com.krince.reminisce.application.port.`in`.auth.command.LogoutCommand
import com.krince.reminisce.application.port.`in`.auth.command.ReissueTokenCommand
import com.krince.reminisce.application.port.out.auth.KakaoOAuthPort
import com.krince.reminisce.application.port.out.auth.KakaoUserInfo
import com.krince.reminisce.application.port.out.auth.PasswordEncoderPort
import com.krince.reminisce.application.port.out.auth.RefreshTokenPort
import com.krince.reminisce.application.port.out.auth.TokenProviderPort
import com.krince.reminisce.application.port.out.user.CommandUserPort
import com.krince.reminisce.application.port.out.user.LoadUserPort
import com.krince.reminisce.domain.model.user.User
import com.krince.reminisce.domain.model.user.vo.AuthProvider
import com.krince.reminisce.domain.model.user.vo.Email
import com.krince.reminisce.domain.model.user.vo.Nickname
import com.krince.reminisce.domain.model.user.vo.Password
import com.krince.reminisce.domain.model.user.vo.Role
import com.krince.reminisce.domain.model.user.vo.UserId
import com.krince.reminisce.shared.exception.UnauthorizedRefreshTokenException
import com.krince.reminisce.shared.response.ExceptionResponseCode.EXPIRED_REFRESH_TOKEN
import com.krince.reminisce.shared.response.ExceptionResponseCode.INVALID_PASSWORD
import com.krince.reminisce.shared.response.ExceptionResponseCode.INVALID_REFRESH_TOKEN
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.mockk.verifyOrder
import org.springframework.security.authentication.BadCredentialsException
import java.time.Duration

@Tags("test", "unitTest")
@DisplayName("AuthApplicationService 단위테스트")
class AuthApplicationServiceTest : FunSpec({

    val loadUserPort = mockk<LoadUserPort>()
    val commandUserPort = mockk<CommandUserPort>()
    val passwordEncoderPort = mockk<PasswordEncoderPort>()
    val tokenProviderPort = mockk<TokenProviderPort>()
    val refreshTokenPort = mockk<RefreshTokenPort>()
    val accessTokenBlacklister = mockk<AccessTokenBlacklister>()
    val kakaoOAuthPort = mockk<KakaoOAuthPort>()
    val service = AuthApplicationService(
        loadUserPort = loadUserPort,
        commandUserPort = commandUserPort,
        passwordEncoderPort = passwordEncoderPort,
        tokenProviderPort = tokenProviderPort,
        refreshTokenPort = refreshTokenPort,
        accessTokenBlacklister = accessTokenBlacklister,
        kakaoOAuthPort = kakaoOAuthPort,
    )

    beforeEach { clearAllMocks() }

    val email = "user@example.com"
    val rawPassword = "Password1!"
    val encodedPassword = "\$2a\$10\$hashedvalue"
    val userIdStr = "user-uuid-1"
    val roleValue = "ROLE_USER"
    val refreshTtl = Duration.ofMillis(1_209_600_000)
    val user = User(
        userId = UserId(userIdStr),
        email = Email(email),
        password = Password(encodedPassword),
        nickname = Nickname("홍길동"),
        provider = AuthProvider.LOCAL,
        role = Role.user(),
    )

    context("LoginUseCase") {
        context("성공") {
            test("이메일 조회·비밀번호 검증 후 토큰을 발급하고 리프레시를 저장한다") {
                every { loadUserPort.findByEmail(Email(email)) } returns user
                every { passwordEncoderPort.matchPassword(rawPassword, encodedPassword) } returns Unit
                every { tokenProviderPort.generateAccessToken(userIdStr, roleValue) } returns "Bearer access"
                every { tokenProviderPort.generateRefreshToken(userIdStr, roleValue) } returns "Bearer refresh"
                every { tokenProviderPort.getRefreshTokenExpiration() } returns refreshTtl
                every { refreshTokenPort.save(userIdStr, "Bearer refresh", refreshTtl) } returns Unit

                val result = service.execute(LoginCommand(email, rawPassword))

                result.accessToken shouldBe "Bearer access"
                result.refreshToken shouldBe "Bearer refresh"
                verifyOrder {
                    loadUserPort.findByEmail(Email(email))
                    passwordEncoderPort.matchPassword(rawPassword, encodedPassword)
                    refreshTokenPort.save(userIdStr, "Bearer refresh", refreshTtl)
                }
            }
        }
        context("실패 - 사용자 열거 방지") {
            test("존재하지 않는 이메일이면 더미 비교로 타이밍을 맞추고 INVALID_PASSWORD와 같은 BadCredentialsException을 던지며 토큰을 발급하지 않는다") {
                every { loadUserPort.findByEmail(Email(email)) } returns null
                every { passwordEncoderPort.matchDummyPassword(rawPassword) } returns Unit

                val exception = shouldThrow<BadCredentialsException> {
                    service.execute(LoginCommand(email, rawPassword))
                }

                exception.message shouldBe INVALID_PASSWORD.message
                verify(exactly = 1) { passwordEncoderPort.matchDummyPassword(rawPassword) }
                verify(exactly = 0) { passwordEncoderPort.matchPassword(any(), any()) }
                verify(exactly = 0) { refreshTokenPort.save(any(), any(), any()) }
            }
            test("비밀번호가 일치하지 않으면 같은 BadCredentialsException을 던지고 토큰을 발급하지 않는다") {
                every { loadUserPort.findByEmail(Email(email)) } returns user
                every { passwordEncoderPort.matchPassword(rawPassword, encodedPassword) } throws
                    BadCredentialsException(INVALID_PASSWORD.message)

                val exception = shouldThrow<BadCredentialsException> {
                    service.execute(LoginCommand(email, rawPassword))
                }

                exception.message shouldBe INVALID_PASSWORD.message
                verify(exactly = 0) { refreshTokenPort.save(any(), any(), any()) }
            }
            test("소셜 계정(provider=KAKAO, password null)에 이메일 로그인 시도하면 더미 비교 후 같은 예외를 던진다") {
                val kakaoUser = User.kakao(providerId = "k-1", email = Email(email), nickname = Nickname("카카오회원"))
                every { loadUserPort.findByEmail(Email(email)) } returns kakaoUser
                every { passwordEncoderPort.matchDummyPassword(rawPassword) } returns Unit

                val exception = shouldThrow<BadCredentialsException> {
                    service.execute(LoginCommand(email, rawPassword))
                }

                exception.message shouldBe INVALID_PASSWORD.message
                verify(exactly = 1) { passwordEncoderPort.matchDummyPassword(rawPassword) }
                verify(exactly = 0) { passwordEncoderPort.matchPassword(any(), any()) }
            }
        }
    }

    context("KakaoLoginUseCase") {
        val authCode = "kakao-auth-code"
        val kakaoId = "kakao-9999"

        context("첫 로그인 - 신규 가입") {
            test("카카오 사용자 조회 후 계정이 없으면 생성하고 우리 토큰을 발급한다") {
                every { kakaoOAuthPort.exchangeCodeForUser(authCode) } returns
                    KakaoUserInfo(id = kakaoId, email = "kakao@example.com", nickname = "카카오회원")
                every { loadUserPort.findByProviderAndProviderId(AuthProvider.KAKAO, kakaoId) } returns null
                val savedSlot = slot<User>()
                every { commandUserPort.save(capture(savedSlot)) } answers { savedSlot.captured }
                every { tokenProviderPort.generateAccessToken(any(), any()) } returns "Bearer access"
                every { tokenProviderPort.generateRefreshToken(any(), any()) } returns "Bearer refresh"
                every { tokenProviderPort.getRefreshTokenExpiration() } returns refreshTtl
                every { refreshTokenPort.save(any(), any(), any()) } returns Unit

                val result = service.execute(KakaoLoginCommand(authCode))

                result.accessToken shouldBe "Bearer access"
                result.refreshToken shouldBe "Bearer refresh"
                savedSlot.captured.provider shouldBe AuthProvider.KAKAO
                savedSlot.captured.providerId shouldBe kakaoId
                savedSlot.captured.password shouldBe null
                verify(exactly = 1) { commandUserPort.save(any()) }
            }
        }
        context("재방문 - 로그인") {
            test("이미 있는 카카오 계정이면 새로 만들지 않고 토큰만 발급한다") {
                every { kakaoOAuthPort.exchangeCodeForUser(authCode) } returns
                    KakaoUserInfo(id = kakaoId, email = null, nickname = "카카오회원")
                every { loadUserPort.findByProviderAndProviderId(AuthProvider.KAKAO, kakaoId) } returns
                    User.kakao(providerId = kakaoId, email = null, nickname = Nickname("카카오회원"))
                every { tokenProviderPort.generateAccessToken(any(), any()) } returns "Bearer access"
                every { tokenProviderPort.generateRefreshToken(any(), any()) } returns "Bearer refresh"
                every { tokenProviderPort.getRefreshTokenExpiration() } returns refreshTtl
                every { refreshTokenPort.save(any(), any(), any()) } returns Unit

                val result = service.execute(KakaoLoginCommand(authCode))

                result.accessToken shouldBe "Bearer access"
                verify(exactly = 0) { commandUserPort.save(any()) }
            }
        }
    }

    context("ReissueTokenUseCase") {
        val providedRefresh = "Bearer stored-refresh"
        val extracted = "stored-refresh"

        context("성공 - 회전") {
            test("저장분과 일치하면 새 토큰을 발급하고 리프레시 저장분을 교체한다") {
                every { tokenProviderPort.extractToken(providedRefresh) } returns extracted
                every { tokenProviderPort.validateRefreshToken(extracted) } returns Unit
                every { tokenProviderPort.getUserId(extracted) } returns userIdStr
                every { tokenProviderPort.getRole(extracted) } returns roleValue
                every { refreshTokenPort.find(userIdStr) } returns providedRefresh
                every { tokenProviderPort.generateAccessToken(userIdStr, roleValue) } returns "Bearer new-access"
                every { tokenProviderPort.generateRefreshToken(userIdStr, roleValue) } returns "Bearer new-refresh"
                every { tokenProviderPort.getRefreshTokenExpiration() } returns refreshTtl
                val savedSlot = slot<String>()
                every { refreshTokenPort.save(userIdStr, capture(savedSlot), refreshTtl) } returns Unit

                val result = service.execute(ReissueTokenCommand(providedRefresh))

                result.accessToken shouldBe "Bearer new-access"
                result.refreshToken shouldBe "Bearer new-refresh"
                savedSlot.captured shouldBe "Bearer new-refresh"
                (savedSlot.captured == providedRefresh) shouldBe false
                verifyOrder {
                    tokenProviderPort.validateRefreshToken(extracted)
                    refreshTokenPort.find(userIdStr)
                    refreshTokenPort.save(userIdStr, "Bearer new-refresh", refreshTtl)
                }
            }
        }
        context("실패 - 저장분 대조") {
            test("저장분이 없으면 INVALID_REFRESH_TOKEN을 던지고 재발급하지 않는다") {
                every { tokenProviderPort.extractToken(providedRefresh) } returns extracted
                every { tokenProviderPort.validateRefreshToken(extracted) } returns Unit
                every { tokenProviderPort.getUserId(extracted) } returns userIdStr
                every { tokenProviderPort.getRole(extracted) } returns roleValue
                every { refreshTokenPort.find(userIdStr) } returns null

                val exception = shouldThrow<UnauthorizedRefreshTokenException> {
                    service.execute(ReissueTokenCommand(providedRefresh))
                }

                exception.exceptionResponseCode shouldBe INVALID_REFRESH_TOKEN
                verify(exactly = 0) { refreshTokenPort.save(any(), any(), any()) }
            }
            test("저장분과 다르면(회전 후 기존 토큰) INVALID_REFRESH_TOKEN을 던진다") {
                every { tokenProviderPort.extractToken(providedRefresh) } returns extracted
                every { tokenProviderPort.validateRefreshToken(extracted) } returns Unit
                every { tokenProviderPort.getUserId(extracted) } returns userIdStr
                every { tokenProviderPort.getRole(extracted) } returns roleValue
                every { refreshTokenPort.find(userIdStr) } returns "Bearer rotated-refresh"

                val exception = shouldThrow<UnauthorizedRefreshTokenException> {
                    service.execute(ReissueTokenCommand(providedRefresh))
                }

                exception.exceptionResponseCode shouldBe INVALID_REFRESH_TOKEN
                verify(exactly = 0) { refreshTokenPort.save(any(), any(), any()) }
            }
        }
    }

    context("LogoutUseCase") {
        val providedAccess = "Bearer access-token"

        test("리프레시 검증 성공 후 refresh 삭제 후 accessTokenBlacklister.blacklist에 위임한다") {
            val providedRefresh = "Bearer stored-refresh"
            val extracted = "stored-refresh"
            every { tokenProviderPort.extractToken(providedRefresh) } returns extracted
            every { tokenProviderPort.validateRefreshToken(extracted) } returns Unit
            every { tokenProviderPort.getUserId(extracted) } returns userIdStr
            every { refreshTokenPort.find(userIdStr) } returns providedRefresh
            every { refreshTokenPort.delete(userIdStr) } returns Unit
            every { accessTokenBlacklister.blacklist(providedAccess) } returns Unit

            service.execute(LogoutCommand(refreshToken = providedRefresh, accessToken = providedAccess))

            verifyOrder {
                refreshTokenPort.delete(userIdStr)
                accessTokenBlacklister.blacklist(providedAccess)
            }
        }

        test("blacklist 위임 중 예외는 전파한다") {
            val providedRefresh = "Bearer stored-refresh"
            val extracted = "stored-refresh"
            every { tokenProviderPort.extractToken(providedRefresh) } returns extracted
            every { tokenProviderPort.validateRefreshToken(extracted) } returns Unit
            every { tokenProviderPort.getUserId(extracted) } returns userIdStr
            every { refreshTokenPort.find(userIdStr) } returns providedRefresh
            every { refreshTokenPort.delete(userIdStr) } returns Unit
            every { accessTokenBlacklister.blacklist(providedAccess) } throws
                org.springframework.dao.DataAccessResourceFailureException("redis down")

            shouldThrow<org.springframework.dao.DataAccessResourceFailureException> {
                service.execute(LogoutCommand(refreshToken = providedRefresh, accessToken = providedAccess))
            }
        }

        test("액세스 헤더가 없어도 refresh 삭제 후 blacklist(null)에 위임한다") {
            val providedRefresh = "Bearer stored-refresh"
            val extracted = "stored-refresh"
            every { tokenProviderPort.extractToken(providedRefresh) } returns extracted
            every { tokenProviderPort.validateRefreshToken(extracted) } returns Unit
            every { tokenProviderPort.getUserId(extracted) } returns userIdStr
            every { refreshTokenPort.find(userIdStr) } returns providedRefresh
            every { refreshTokenPort.delete(userIdStr) } returns Unit
            every { accessTokenBlacklister.blacklist(null) } returns Unit

            service.execute(LogoutCommand(refreshToken = providedRefresh, accessToken = null))

            verify(exactly = 1) { refreshTokenPort.delete(userIdStr) }
            verify(exactly = 1) { accessTokenBlacklister.blacklist(null) }
        }

        test("제공된 리프레시가 저장분과 다르면 INVALID_REFRESH_TOKEN을 던지고 blacklister를 호출하지 않는다") {
            val providedRefresh = "Bearer old-refresh"
            val extracted = "old-refresh"
            every { tokenProviderPort.extractToken(providedRefresh) } returns extracted
            every { tokenProviderPort.validateRefreshToken(extracted) } returns Unit
            every { tokenProviderPort.getUserId(extracted) } returns userIdStr
            every { refreshTokenPort.find(userIdStr) } returns "Bearer current-refresh"

            val exception = shouldThrow<UnauthorizedRefreshTokenException> {
                service.execute(LogoutCommand(refreshToken = providedRefresh, accessToken = providedAccess))
            }

            exception.exceptionResponseCode shouldBe INVALID_REFRESH_TOKEN
            verify(exactly = 0) { refreshTokenPort.delete(any()) }
            verify(exactly = 0) { accessTokenBlacklister.blacklist(any()) }
        }

        test("만료·손상된 리프레시면 검증에서 URT 예외를 던지고 blacklister를 호출하지 않는다") {
            val providedRefresh = "Bearer broken-refresh"
            val extracted = "broken-refresh"
            every { tokenProviderPort.extractToken(providedRefresh) } returns extracted
            every { tokenProviderPort.validateRefreshToken(extracted) } throws
                UnauthorizedRefreshTokenException(EXPIRED_REFRESH_TOKEN, EXPIRED_REFRESH_TOKEN.message)

            val exception = shouldThrow<UnauthorizedRefreshTokenException> {
                service.execute(LogoutCommand(refreshToken = providedRefresh, accessToken = providedAccess))
            }

            exception.exceptionResponseCode shouldBe EXPIRED_REFRESH_TOKEN
            verify(exactly = 0) { refreshTokenPort.delete(any()) }
            verify(exactly = 0) { accessTokenBlacklister.blacklist(any()) }
        }

        test("Bearer 접두어가 없으면 INVALID_REFRESH_TOKEN을 던지고 blacklister를 호출하지 않는다") {
            val providedRefresh = "no-prefix-refresh"
            every { tokenProviderPort.extractToken(providedRefresh) } throws IllegalArgumentException("bad prefix")

            val exception = shouldThrow<UnauthorizedRefreshTokenException> {
                service.execute(LogoutCommand(refreshToken = providedRefresh, accessToken = providedAccess))
            }

            exception.exceptionResponseCode shouldBe INVALID_REFRESH_TOKEN
            verify(exactly = 0) { refreshTokenPort.delete(any()) }
            verify(exactly = 0) { accessTokenBlacklister.blacklist(any()) }
        }
    }

    context("ReissueTokenUseCase - 형식 오류") {
        test("Bearer 접두어가 없으면 INVALID_REFRESH_TOKEN을 던지고 재발급하지 않는다") {
            val providedRefresh = "no-prefix-refresh"
            every { tokenProviderPort.extractToken(providedRefresh) } throws IllegalArgumentException("bad prefix")

            val exception = shouldThrow<UnauthorizedRefreshTokenException> {
                service.execute(ReissueTokenCommand(providedRefresh))
            }

            exception.exceptionResponseCode shouldBe INVALID_REFRESH_TOKEN
            verify(exactly = 0) { refreshTokenPort.save(any(), any(), any()) }
        }
    }
})
