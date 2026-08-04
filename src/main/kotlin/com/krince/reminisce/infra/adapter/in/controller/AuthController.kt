package com.krince.reminisce.infra.adapter.`in`.controller

import com.krince.reminisce.infra.adapter.`in`.dto.auth.request.KakaoLoginRequest
import com.krince.reminisce.infra.adapter.`in`.dto.auth.request.LoginRequest
import com.krince.reminisce.infra.swagger.ExceptionExample
import com.krince.reminisce.infra.swagger.SwaggerExceptionResponse
import com.krince.reminisce.infra.swagger.SwaggerSuccessResponse
import com.krince.reminisce.shared.response.ExceptionResponseCode.*
import com.krince.reminisce.shared.response.SuccessResponseCode.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody

@Tag(name = "인증(Auth)")
interface AuthController {

    @Operation(summary = "이메일 로그인", description = "이메일과 비밀번호로 로그인해 액세스·리프레시 토큰을 헤더로 발급합니다.")
    @SwaggerSuccessResponse(responseCode = OK, description = "로그인 성공")
    @SwaggerExceptionResponse(
        examples = [
            ExceptionExample(code = INVALID_DTO_PARAMETER, name = "요청 값 오류", message = "요청 값이 올바르지 않습니다.(dto 검증 오류)", description = "요청 바디 검증에 실패한 경우"),
            ExceptionExample(code = INVALID_EMAIL_FORMAT, name = "이메일 형식 오류", message = "올바르지 않은 이메일 형식입니다.", description = "이메일 형식이 올바르지 않은 경우"),
            ExceptionExample(code = INVALID_PASSWORD, name = "자격증명 오류", message = "비밀번호를 확인해주세요.", description = "이메일이 없거나 비밀번호가 일치하지 않는 경우"),
            ExceptionExample(code = INTERNAL_SERVER_ERROR, name = "서버 오류", message = "서버 에러입니다. 개발자에게 문의해주세요.", description = "예상치 못한 서버 오류가 발생한 경우"),
        ]
    )
    fun login(
        @Valid @RequestBody request: LoginRequest,
    ): ResponseEntity<Void>

    @Operation(summary = "카카오 로그인", description = "카카오 인가코드로 계정을 upsert하고 액세스·리프레시 토큰을 헤더로 발급합니다.")
    @SwaggerSuccessResponse(responseCode = OK, description = "카카오 로그인 성공")
    @SwaggerExceptionResponse(
        examples = [
            ExceptionExample(code = INVALID_DTO_PARAMETER, name = "요청 값 오류", message = "요청 값이 올바르지 않습니다.(dto 검증 오류)", description = "요청 바디 검증에 실패한 경우"),
            ExceptionExample(code = SOCIAL_AUTH_FAILED, name = "소셜 인증 실패", message = "소셜 인증에 실패했습니다.", description = "카카오 코드 교환·사용자 조회에 실패한 경우"),
            ExceptionExample(code = INTERNAL_SERVER_ERROR, name = "서버 오류", message = "서버 에러입니다. 개발자에게 문의해주세요.", description = "예상치 못한 서버 오류가 발생한 경우"),
        ]
    )
    fun kakaoLogin(
        @Valid @RequestBody request: KakaoLoginRequest,
    ): ResponseEntity<Void>

    @Operation(summary = "토큰 재발급", description = "리프레시 토큰으로 액세스·리프레시 토큰을 회전 발급합니다.")
    @SwaggerSuccessResponse(responseCode = OK, description = "토큰 재발급 성공")
    @SwaggerExceptionResponse(
        examples = [
            ExceptionExample(code = EMPTY_REFRESH_TOKEN, name = "리프레시 토큰 없음", message = "리프레시 토큰이 없습니다.", description = "리프레시 토큰 헤더가 제공되지 않은 경우"),
            ExceptionExample(code = UNAUTHORIZED_REFRESH_TOKEN, name = "리프레시 토큰 오류", message = "리프레시 토큰 정보가 올바르지 않습니다.", description = "리프레시 토큰 타입이 올바르지 않은 경우"),
            ExceptionExample(code = EXPIRED_REFRESH_TOKEN, name = "리프레시 토큰 만료", message = "만료된 리프레시 토큰입니다.", description = "리프레시 토큰이 만료된 경우"),
            ExceptionExample(code = INVALID_REFRESH_TOKEN, name = "유효하지 않은 리프레시 토큰", message = "유효하지 않은 리프레시 토큰입니다.", description = "형식이 잘못되었거나 저장분과 일치하지 않는 경우"),
            ExceptionExample(code = INTERNAL_SERVER_ERROR, name = "서버 오류", message = "서버 에러입니다. 개발자에게 문의해주세요.", description = "예상치 못한 서버 오류가 발생한 경우"),
        ]
    )
    fun reissue(
        refreshToken: String?,
    ): ResponseEntity<Void>

    @Operation(summary = "로그아웃", description = "리프레시 토큰을 무효화하고 Authorization 헤더의 액세스 토큰을 블랙리스트에 등록해 로그아웃합니다. 액세스 토큰 헤더는 선택이며 없거나 만료된 경우 리프레시 무효화만 수행합니다.")
    @SwaggerSuccessResponse(responseCode = NO_CONTENT, description = "로그아웃 성공")
    @SwaggerExceptionResponse(
        examples = [
            ExceptionExample(code = EMPTY_REFRESH_TOKEN, name = "리프레시 토큰 없음", message = "리프레시 토큰이 없습니다.", description = "리프레시 토큰 헤더가 제공되지 않은 경우"),
            ExceptionExample(code = UNAUTHORIZED_REFRESH_TOKEN, name = "리프레시 토큰 오류", message = "리프레시 토큰 정보가 올바르지 않습니다.", description = "리프레시 토큰 타입이 올바르지 않은 경우"),
            ExceptionExample(code = EXPIRED_REFRESH_TOKEN, name = "리프레시 토큰 만료", message = "만료된 리프레시 토큰입니다.", description = "리프레시 토큰이 만료된 경우"),
            ExceptionExample(code = INVALID_REFRESH_TOKEN, name = "유효하지 않은 리프레시 토큰", message = "유효하지 않은 리프레시 토큰입니다.", description = "형식이 잘못되었거나 저장분과 일치하지 않는 경우"),
            ExceptionExample(code = LOGGED_OUT_TOKEN, name = "로그아웃된 토큰", message = "로그아웃된 토큰입니다.", description = "로그아웃되어 블랙리스트에 등록된 액세스 토큰으로 인증 API를 재요청한 경우"),
            ExceptionExample(code = INTERNAL_SERVER_ERROR, name = "서버 오류", message = "서버 에러입니다. 개발자에게 문의해주세요.", description = "예상치 못한 서버 오류가 발생한 경우"),
        ]
    )
    fun logout(
        refreshToken: String?,
        accessToken: String?,
    ): ResponseEntity<Void>
}
