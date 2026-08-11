package com.krince.reminisce.application.service.auth

import com.krince.reminisce.application.port.`in`.auth.command.GoogleLoginCommand
import com.krince.reminisce.application.port.`in`.auth.command.KakaoLoginCommand
import com.krince.reminisce.application.port.`in`.auth.command.LogoutCommand
import com.krince.reminisce.application.port.`in`.auth.command.NaverLoginCommand
import com.krince.reminisce.application.port.`in`.auth.command.ReissueTokenCommand
import com.krince.reminisce.application.port.out.auth.GoogleOAuthPort
import com.krince.reminisce.application.port.out.auth.GoogleUserInfo
import com.krince.reminisce.application.port.out.auth.KakaoOAuthPort
import com.krince.reminisce.application.port.out.auth.KakaoUserInfo
import com.krince.reminisce.application.port.out.auth.NaverOAuthPort
import com.krince.reminisce.application.port.out.auth.NaverUserInfo
import com.krince.reminisce.application.port.out.auth.RefreshTokenPort
import com.krince.reminisce.application.port.out.auth.TokenProviderPort
import com.krince.reminisce.application.port.out.user.CommandUserPort
import com.krince.reminisce.application.port.out.user.LoadUserPort
import com.krince.reminisce.domain.model.user.User
import com.krince.reminisce.domain.model.user.vo.AuthProvider
import com.krince.reminisce.domain.model.user.vo.Nickname
import com.krince.reminisce.shared.exception.UnauthorizedRefreshTokenException
import com.krince.reminisce.shared.response.ExceptionResponseCode.EXPIRED_REFRESH_TOKEN
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
import java.time.Duration

@Tags("test", "unitTest")
@DisplayName("AuthApplicationService 단위테스트")
class AuthApplicationServiceTest : FunSpec({

    val loadUserPort = mockk<LoadUserPort>()
    val commandUserPort = mockk<CommandUserPort>()
    val tokenProviderPort = mockk<TokenProviderPort>()
    val refreshTokenPort = mockk<RefreshTokenPort>()
    val accessTokenBlacklister = mockk<AccessTokenBlacklister>()
    val kakaoOAuthPort = mockk<KakaoOAuthPort>()
    val googleOAuthPort = mockk<GoogleOAuthPort>()
    val naverOAuthPort = mockk<NaverOAuthPort>()
    val service = AuthApplicationService(
        loadUserPort = loadUserPort,
        commandUserPort = commandUserPort,
        tokenProviderPort = tokenProviderPort,
        refreshTokenPort = refreshTokenPort,
        accessTokenBlacklister = accessTokenBlacklister,
        kakaoOAuthPort = kakaoOAuthPort,
        googleOAuthPort = googleOAuthPort,
        naverOAuthPort = naverOAuthPort,
    )

    beforeEach { clearAllMocks() }

    val userIdStr = "user-uuid-1"
    val roleValue = "ROLE_USER"
    val refreshTtl = Duration.ofMillis(1_209_600_000)

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
                savedSlot.captured.email?.value shouldBe "kakao@example.com"
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

    context("GoogleLoginUseCase") {
        val authCode = "google-auth-code"
        val googleId = "google-sub-9999"

        context("첫 로그인 - 신규 가입") {
            test("구글 사용자 조회 후 계정이 없으면 생성하고 우리 토큰을 발급한다") {
                every { googleOAuthPort.exchangeCodeForUser(authCode) } returns
                    GoogleUserInfo(id = googleId, email = "google@example.com", nickname = "구글회원")
                every { loadUserPort.findByProviderAndProviderId(AuthProvider.GOOGLE, googleId) } returns null
                val savedSlot = slot<User>()
                every { commandUserPort.save(capture(savedSlot)) } answers { savedSlot.captured }
                every { tokenProviderPort.generateAccessToken(any(), any()) } returns "Bearer access"
                every { tokenProviderPort.generateRefreshToken(any(), any()) } returns "Bearer refresh"
                every { tokenProviderPort.getRefreshTokenExpiration() } returns refreshTtl
                every { refreshTokenPort.save(any(), any(), any()) } returns Unit

                val result = service.execute(GoogleLoginCommand(authCode))

                result.accessToken shouldBe "Bearer access"
                result.refreshToken shouldBe "Bearer refresh"
                savedSlot.captured.provider shouldBe AuthProvider.GOOGLE
                savedSlot.captured.providerId shouldBe googleId
                savedSlot.captured.email?.value shouldBe "google@example.com"
                verify(exactly = 1) { commandUserPort.save(any()) }
            }
        }
        context("재방문 - 로그인") {
            test("이미 있는 구글 계정이면 새로 만들지 않고 토큰만 발급한다") {
                every { googleOAuthPort.exchangeCodeForUser(authCode) } returns
                    GoogleUserInfo(id = googleId, email = null, nickname = "구글회원")
                every { loadUserPort.findByProviderAndProviderId(AuthProvider.GOOGLE, googleId) } returns
                    User.google(providerId = googleId, email = null, nickname = Nickname("구글회원"))
                every { tokenProviderPort.generateAccessToken(any(), any()) } returns "Bearer access"
                every { tokenProviderPort.generateRefreshToken(any(), any()) } returns "Bearer refresh"
                every { tokenProviderPort.getRefreshTokenExpiration() } returns refreshTtl
                every { refreshTokenPort.save(any(), any(), any()) } returns Unit

                val result = service.execute(GoogleLoginCommand(authCode))

                result.accessToken shouldBe "Bearer access"
                verify(exactly = 0) { commandUserPort.save(any()) }
            }
        }
    }

    context("NaverLoginUseCase") {
        val authCode = "naver-auth-code"
        val state = "naver-state"
        val naverId = "naver-id-9999"

        context("첫 로그인 - 신규 가입") {
            test("네이버 사용자 조회 후 계정이 없으면 생성하고 우리 토큰을 발급한다") {
                every { naverOAuthPort.exchangeCodeForUser(authCode, state) } returns
                    NaverUserInfo(id = naverId, email = "naver@example.com", nickname = "네이버회원")
                every { loadUserPort.findByProviderAndProviderId(AuthProvider.NAVER, naverId) } returns null
                val savedSlot = slot<User>()
                every { commandUserPort.save(capture(savedSlot)) } answers { savedSlot.captured }
                every { tokenProviderPort.generateAccessToken(any(), any()) } returns "Bearer access"
                every { tokenProviderPort.generateRefreshToken(any(), any()) } returns "Bearer refresh"
                every { tokenProviderPort.getRefreshTokenExpiration() } returns refreshTtl
                every { refreshTokenPort.save(any(), any(), any()) } returns Unit

                val result = service.execute(NaverLoginCommand(authCode, state))

                result.accessToken shouldBe "Bearer access"
                result.refreshToken shouldBe "Bearer refresh"
                savedSlot.captured.provider shouldBe AuthProvider.NAVER
                savedSlot.captured.providerId shouldBe naverId
                savedSlot.captured.email?.value shouldBe "naver@example.com"
                verify(exactly = 1) { commandUserPort.save(any()) }
            }
        }
        context("재방문 - 로그인") {
            test("이미 있는 네이버 계정이면 새로 만들지 않고 토큰만 발급한다") {
                every { naverOAuthPort.exchangeCodeForUser(authCode, state) } returns
                    NaverUserInfo(id = naverId, email = null, nickname = "네이버회원")
                every { loadUserPort.findByProviderAndProviderId(AuthProvider.NAVER, naverId) } returns
                    User.naver(providerId = naverId, email = null, nickname = Nickname("네이버회원"))
                every { tokenProviderPort.generateAccessToken(any(), any()) } returns "Bearer access"
                every { tokenProviderPort.generateRefreshToken(any(), any()) } returns "Bearer refresh"
                every { tokenProviderPort.getRefreshTokenExpiration() } returns refreshTtl
                every { refreshTokenPort.save(any(), any(), any()) } returns Unit

                val result = service.execute(NaverLoginCommand(authCode, state))

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
