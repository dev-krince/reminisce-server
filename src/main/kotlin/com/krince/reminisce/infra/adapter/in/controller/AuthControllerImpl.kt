package com.krince.reminisce.infra.adapter.`in`.controller

import com.krince.reminisce.application.port.`in`.auth.command.GoogleLoginCommand
import com.krince.reminisce.application.port.`in`.auth.command.KakaoLoginCommand
import com.krince.reminisce.application.port.`in`.auth.command.LoginCommand
import com.krince.reminisce.application.port.`in`.auth.command.LogoutCommand
import com.krince.reminisce.application.port.`in`.auth.command.ReissueTokenCommand
import com.krince.reminisce.application.port.`in`.auth.result.TokenResult
import com.krince.reminisce.application.port.`in`.auth.usecase.GoogleLoginUseCase
import com.krince.reminisce.application.port.`in`.auth.usecase.KakaoLoginUseCase
import com.krince.reminisce.application.port.`in`.auth.usecase.LoginUseCase
import com.krince.reminisce.application.port.`in`.auth.usecase.LogoutUseCase
import com.krince.reminisce.application.port.`in`.auth.usecase.ReissueTokenUseCase
import com.krince.reminisce.infra.adapter.`in`.dto.auth.request.GoogleLoginRequest
import com.krince.reminisce.infra.adapter.`in`.dto.auth.request.KakaoLoginRequest
import com.krince.reminisce.infra.adapter.`in`.dto.auth.request.LoginRequest
import com.krince.reminisce.shared.exception.UnauthorizedRefreshTokenException
import com.krince.reminisce.shared.response.ExceptionResponseCode.EMPTY_REFRESH_TOKEN
import com.krince.reminisce.shared.response.SuccessResponseCode.NO_CONTENT
import com.krince.reminisce.shared.response.SuccessResponseCode.OK
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/api/auth")
class AuthControllerImpl(
    private val loginUseCase: LoginUseCase,
    private val kakaoLoginUseCase: KakaoLoginUseCase,
    private val googleLoginUseCase: GoogleLoginUseCase,
    private val reissueTokenUseCase: ReissueTokenUseCase,
    private val logoutUseCase: LogoutUseCase,
) : AuthController {

    @PostMapping("/tokens")
    override fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<Void> {
        val command = LoginCommand(email = request.email, password = request.password)
        val result: TokenResult = loginUseCase.execute(command)

        return ResponseEntity.status(OK.code)
            .header(ACCESS_TOKEN_HEADER_NAME, result.accessToken)
            .header(REFRESH_TOKEN_HEADER_NAME, result.refreshToken)
            .build()
    }

    @PostMapping("/tokens/kakao")
    override fun kakaoLogin(@Valid @RequestBody request: KakaoLoginRequest): ResponseEntity<Void> {
        val command = KakaoLoginCommand(authorizationCode = request.authorizationCode)
        val result: TokenResult = kakaoLoginUseCase.execute(command)

        return ResponseEntity.status(OK.code)
            .header(ACCESS_TOKEN_HEADER_NAME, result.accessToken)
            .header(REFRESH_TOKEN_HEADER_NAME, result.refreshToken)
            .build()
    }

    @PostMapping("/tokens/google")
    override fun googleLogin(@Valid @RequestBody request: GoogleLoginRequest): ResponseEntity<Void> {
        val command = GoogleLoginCommand(authorizationCode = request.authorizationCode)
        val result: TokenResult = googleLoginUseCase.execute(command)

        return ResponseEntity.status(OK.code)
            .header(ACCESS_TOKEN_HEADER_NAME, result.accessToken)
            .header(REFRESH_TOKEN_HEADER_NAME, result.refreshToken)
            .build()
    }

    @PostMapping("/tokens/refresh")
    override fun reissue(
        @RequestHeader(name = REFRESH_TOKEN_HEADER_NAME, required = false) refreshToken: String?,
    ): ResponseEntity<Void> {
        val providedToken: String = requireRefreshToken(refreshToken)
        val command = ReissueTokenCommand(refreshToken = providedToken)
        val result: TokenResult = reissueTokenUseCase.execute(command)

        return ResponseEntity.status(OK.code)
            .header(ACCESS_TOKEN_HEADER_NAME, result.accessToken)
            .header(REFRESH_TOKEN_HEADER_NAME, result.refreshToken)
            .build()
    }

    @DeleteMapping("/tokens")
    override fun logout(
        @RequestHeader(name = REFRESH_TOKEN_HEADER_NAME, required = false) refreshToken: String?,
        @RequestHeader(name = ACCESS_TOKEN_HEADER_NAME, required = false) accessToken: String?,
    ): ResponseEntity<Void> {
        val providedToken: String = requireRefreshToken(refreshToken)
        val command = LogoutCommand(refreshToken = providedToken, accessToken = accessToken)
        logoutUseCase.execute(command)

        return ResponseEntity.status(NO_CONTENT.code).build()
    }

    private fun requireRefreshToken(refreshToken: String?): String =
        refreshToken?.takeIf { it.isNotBlank() }
            ?: throw UnauthorizedRefreshTokenException(EMPTY_REFRESH_TOKEN, EMPTY_REFRESH_TOKEN.message)

    companion object {
        private const val ACCESS_TOKEN_HEADER_NAME = "Authorization"
        private const val REFRESH_TOKEN_HEADER_NAME = "refreshToken"
    }
}
