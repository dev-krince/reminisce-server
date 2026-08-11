package com.krince.reminisce.application.service.auth

import com.krince.reminisce.application.port.`in`.auth.command.GoogleLoginCommand
import com.krince.reminisce.application.port.`in`.auth.command.KakaoLoginCommand
import com.krince.reminisce.application.port.`in`.auth.command.LogoutCommand
import com.krince.reminisce.application.port.`in`.auth.command.NaverLoginCommand
import com.krince.reminisce.application.port.`in`.auth.command.ReissueTokenCommand
import com.krince.reminisce.application.port.`in`.auth.result.TokenResult
import com.krince.reminisce.application.port.`in`.auth.usecase.GoogleLoginUseCase
import com.krince.reminisce.application.port.`in`.auth.usecase.KakaoLoginUseCase
import com.krince.reminisce.application.port.`in`.auth.usecase.LogoutUseCase
import com.krince.reminisce.application.port.`in`.auth.usecase.NaverLoginUseCase
import com.krince.reminisce.application.port.`in`.auth.usecase.ReissueTokenUseCase
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
import com.krince.reminisce.application.validator.auth.ReissueTokenValidator
import com.krince.reminisce.domain.model.user.User
import com.krince.reminisce.domain.model.user.vo.AuthProvider
import com.krince.reminisce.domain.model.user.vo.Email
import com.krince.reminisce.domain.model.user.vo.Nickname
import com.krince.reminisce.shared.exception.UnauthorizedRefreshTokenException
import com.krince.reminisce.shared.response.ExceptionResponseCode.INVALID_REFRESH_TOKEN
import org.springframework.stereotype.Service

@Service
class AuthApplicationService(
    private val loadUserPort: LoadUserPort,
    private val commandUserPort: CommandUserPort,
    private val tokenProviderPort: TokenProviderPort,
    private val refreshTokenPort: RefreshTokenPort,
    private val accessTokenBlacklister: AccessTokenBlacklister,
    private val kakaoOAuthPort: KakaoOAuthPort,
    private val googleOAuthPort: GoogleOAuthPort,
    private val naverOAuthPort: NaverOAuthPort,
) : ReissueTokenUseCase, LogoutUseCase, KakaoLoginUseCase, GoogleLoginUseCase, NaverLoginUseCase {

    override fun execute(command: KakaoLoginCommand): TokenResult {
        val kakaoUser: KakaoUserInfo = kakaoOAuthPort.exchangeCodeForUser(command.authorizationCode)
        val user: User = loadUserPort.findByProviderAndProviderId(AuthProvider.KAKAO, kakaoUser.id)
            ?: registerKakaoUser(kakaoUser)

        return issueTokens(userId = user.userId.value, role = user.role.value)
    }

    override fun execute(command: GoogleLoginCommand): TokenResult {
        val googleUser: GoogleUserInfo = googleOAuthPort.exchangeCodeForUser(command.authorizationCode)
        val user: User = loadUserPort.findByProviderAndProviderId(AuthProvider.GOOGLE, googleUser.id)
            ?: registerGoogleUser(googleUser)

        return issueTokens(userId = user.userId.value, role = user.role.value)
    }

    override fun execute(command: NaverLoginCommand): TokenResult {
        val naverUser: NaverUserInfo = naverOAuthPort.exchangeCodeForUser(command.authorizationCode, command.state)
        val user: User = loadUserPort.findByProviderAndProviderId(AuthProvider.NAVER, naverUser.id)
            ?: registerNaverUser(naverUser)

        return issueTokens(userId = user.userId.value, role = user.role.value)
    }

    override fun execute(command: ReissueTokenCommand): TokenResult {
        val extractedToken: String = extractRefreshToken(command.refreshToken)
        tokenProviderPort.validateRefreshToken(extractedToken)

        val userId: String = tokenProviderPort.getUserId(extractedToken)
        val role: String = tokenProviderPort.getRole(extractedToken)
        val storedToken: String? = refreshTokenPort.find(userId)
        ReissueTokenValidator.validateMatches(command.refreshToken, storedToken)

        return issueTokens(userId = userId, role = role)
    }

    override fun execute(command: LogoutCommand) {
        val extractedToken: String = extractRefreshToken(command.refreshToken)
        tokenProviderPort.validateRefreshToken(extractedToken)
        val userId: String = tokenProviderPort.getUserId(extractedToken)
        val storedToken: String? = refreshTokenPort.find(userId)
        ReissueTokenValidator.validateMatches(command.refreshToken, storedToken)

        refreshTokenPort.delete(userId)
        accessTokenBlacklister.blacklist(command.accessToken)
    }

    private fun registerKakaoUser(kakaoUser: KakaoUserInfo): User {
        val user: User = User.kakao(
            providerId = kakaoUser.id,
            email = kakaoUser.email?.let { Email(it) },
            nickname = Nickname(kakaoUser.nickname),
        )

        return commandUserPort.save(user)
    }

    private fun registerGoogleUser(googleUser: GoogleUserInfo): User {
        val user: User = User.google(
            providerId = googleUser.id,
            email = googleUser.email?.let { Email(it) },
            nickname = Nickname(googleUser.nickname),
        )

        return commandUserPort.save(user)
    }

    private fun registerNaverUser(naverUser: NaverUserInfo): User {
        val user: User = User.naver(
            providerId = naverUser.id,
            email = naverUser.email?.let { Email(it) },
            nickname = Nickname(naverUser.nickname),
        )

        return commandUserPort.save(user)
    }

    private fun extractRefreshToken(rawToken: String): String =
        try {
            tokenProviderPort.extractToken(rawToken)
        } catch (_: IllegalArgumentException) {
            throw UnauthorizedRefreshTokenException(INVALID_REFRESH_TOKEN, INVALID_REFRESH_TOKEN.message)
        }

    private fun issueTokens(userId: String, role: String): TokenResult {
        val accessToken: String = tokenProviderPort.generateAccessToken(userId, role)
        val refreshToken: String = tokenProviderPort.generateRefreshToken(userId, role)
        refreshTokenPort.save(userId, refreshToken, tokenProviderPort.getRefreshTokenExpiration())

        return TokenResult(accessToken = accessToken, refreshToken = refreshToken)
    }
}
