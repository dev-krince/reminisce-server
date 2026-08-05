package com.krince.reminisce.infra.adapter.`in`.controller

import com.krince.reminisce.infra.adapter.`in`.dto.user.request.ConfirmEmailVerificationRequest
import com.krince.reminisce.infra.adapter.`in`.dto.user.request.SendEmailVerificationRequest
import com.krince.reminisce.infra.adapter.`in`.dto.user.request.SignUpRequest
import com.krince.reminisce.infra.adapter.`in`.dto.user.response.SignUpResponse
import com.krince.reminisce.infra.adapter.`in`.dto.user.response.UserResponse
import com.krince.reminisce.infra.security.CustomUserDetails
import com.krince.reminisce.infra.swagger.ExceptionExample
import com.krince.reminisce.infra.swagger.SwaggerExceptionResponse
import com.krince.reminisce.infra.swagger.SwaggerSuccessResponse
import com.krince.reminisce.shared.response.ExceptionResponseCode.*
import com.krince.reminisce.shared.response.SuccessResponse
import com.krince.reminisce.shared.response.SuccessResponseCode.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader

@Tag(name = "회원(Users)")
interface UserController {

    //[POST] /api/users
    @Operation(summary = "이메일 회원가입", description = "이메일 인증 완료 후 회원 계정을 생성합니다.")
    @SwaggerSuccessResponse(responseCode = CREATED, description = "회원가입 성공")
    @SwaggerExceptionResponse(
        examples = [
            ExceptionExample(code = INVALID_DTO_PARAMETER, name = "요청 값 오류", message = "요청 값이 올바르지 않습니다.(dto 검증 오류)", description = "요청 바디 검증에 실패한 경우"),
            ExceptionExample(code = INVALID_EMAIL_FORMAT, name = "이메일 형식 오류", message = "올바르지 않은 이메일 형식입니다.", description = "이메일 형식이 올바르지 않은 경우"),
            ExceptionExample(code = INVALID_PASSWORD_FORMAT, name = "비밀번호 형식 오류", message = "비밀번호는 8자 이상이며 영문, 숫자, 특수문자를 포함해야 합니다.", description = "비밀번호가 정책을 만족하지 않는 경우"),
            ExceptionExample(code = EMAIL_NOT_VERIFIED, name = "이메일 미인증", message = "이메일 인증이 완료되지 않았습니다.", description = "이메일 인증을 완료하지 않고 가입을 시도한 경우"),
            ExceptionExample(code = DUPLICATE_EMAIL, name = "이메일 중복", message = "이미 사용 중인 이메일입니다.", description = "이미 가입된 이메일로 가입을 시도한 경우"),
            ExceptionExample(code = INTERNAL_SERVER_ERROR, name = "서버 오류", message = "서버 에러입니다. 개발자에게 문의해주세요.", description = "예상치 못한 서버 오류가 발생한 경우"),
        ]
    )
    fun signUp(
        @Valid @RequestBody request: SignUpRequest,
    ): ResponseEntity<SuccessResponse<SignUpResponse>>

    //[POST] /api/users/email-verifications
    @Operation(summary = "이메일 인증코드 발송", description = "가입할 이메일로 인증코드를 발송합니다.")
    @SwaggerSuccessResponse(responseCode = NO_CONTENT, description = "인증코드 발송 성공")
    @SwaggerExceptionResponse(
        examples = [
            ExceptionExample(code = INVALID_DTO_PARAMETER, name = "요청 값 오류", message = "요청 값이 올바르지 않습니다.(dto 검증 오류)", description = "요청 바디 검증에 실패한 경우"),
            ExceptionExample(code = INVALID_EMAIL_FORMAT, name = "이메일 형식 오류", message = "올바르지 않은 이메일 형식입니다.", description = "이메일 형식이 올바르지 않은 경우"),
            ExceptionExample(code = DUPLICATE_EMAIL, name = "이메일 중복", message = "이미 사용 중인 이메일입니다.", description = "이미 가입된 이메일로 발송을 시도한 경우"),
            ExceptionExample(code = INTERNAL_SERVER_ERROR, name = "서버 오류", message = "서버 에러입니다. 개발자에게 문의해주세요.", description = "예상치 못한 서버 오류가 발생한 경우"),
        ]
    )
    fun sendEmailVerification(
        @Valid @RequestBody request: SendEmailVerificationRequest,
    ): ResponseEntity<Void>

    //[POST] /api/users/email-verifications/confirm
    @Operation(summary = "이메일 인증코드 확인", description = "발송된 인증코드를 확인해 인증 상태로 전환합니다.")
    @SwaggerSuccessResponse(responseCode = NO_CONTENT, description = "인증코드 확인 성공")
    @SwaggerExceptionResponse(
        examples = [
            ExceptionExample(code = INVALID_DTO_PARAMETER, name = "요청 값 오류", message = "요청 값이 올바르지 않습니다.(dto 검증 오류)", description = "요청 바디 검증에 실패한 경우"),
            ExceptionExample(code = INVALID_VERIFICATION_CODE, name = "인증코드 불일치", message = "인증코드가 일치하지 않습니다.", description = "입력한 인증코드가 저장된 코드와 다른 경우"),
            ExceptionExample(code = EXPIRED_VERIFICATION_CODE, name = "인증코드 만료", message = "인증코드가 만료되었습니다.", description = "인증코드가 만료되었거나 존재하지 않는 경우"),
            ExceptionExample(code = INTERNAL_SERVER_ERROR, name = "서버 오류", message = "서버 에러입니다. 개발자에게 문의해주세요.", description = "예상치 못한 서버 오류가 발생한 경우"),
        ]
    )
    fun confirmEmailVerification(
        @Valid @RequestBody request: ConfirmEmailVerificationRequest,
    ): ResponseEntity<Void>

    //[GET] /api/users/me
    @Operation(summary = "내 정보 조회", description = "토큰의 본인 식별자로 로그인한 회원 본인의 상세 정보를 조회합니다.")
    @SwaggerSuccessResponse(responseCode = OK, description = "회원 조회 성공")
    @SwaggerExceptionResponse(
        examples = [
            ExceptionExample(code = EMPTY_TOKEN, name = "토큰 없음", message = "토큰이 없습니다.", description = "인증 토큰이 제공되지 않은 경우"),
            ExceptionExample(code = INVALID_TOKEN, name = "유효하지 않은 토큰", message = "유효하지 않은 토큰입니다.", description = "토큰이 유효하지 않거나 서명이 잘못된 경우"),
            ExceptionExample(code = EXPIRED_TOKEN, name = "만료된 토큰", message = "만료된 토큰입니다.", description = "토큰의 유효기간이 만료된 경우"),
            ExceptionExample(code = NOT_FOUND_USER, name = "회원 없음", message = "회원이 존재하지 않습니다.", description = "조회하려는 회원이 존재하지 않는 경우"),
            ExceptionExample(code = INTERNAL_SERVER_ERROR, name = "서버 오류", message = "서버 에러입니다. 개발자에게 문의해주세요.", description = "예상치 못한 서버 오류가 발생한 경우"),
        ]
    )
    fun getUser(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<SuccessResponse<UserResponse>>

    @Operation(summary = "회원 탈퇴", description = "본인 계정과 딸린 아이·동의 데이터를 즉시 파기하고 세션 상태를 정리합니다.")
    @SwaggerSuccessResponse(responseCode = NO_CONTENT, description = "회원 탈퇴 성공")
    @SwaggerExceptionResponse(
        examples = [
            ExceptionExample(code = EMPTY_TOKEN, name = "토큰 없음", message = "토큰이 없습니다.", description = "인증 토큰이 제공되지 않은 경우"),
            ExceptionExample(code = INVALID_TOKEN, name = "유효하지 않은 토큰", message = "유효하지 않은 토큰입니다.", description = "토큰이 유효하지 않거나 서명이 잘못된 경우"),
            ExceptionExample(code = EXPIRED_TOKEN, name = "만료된 토큰", message = "만료된 토큰입니다.", description = "토큰의 유효기간이 만료된 경우"),
            ExceptionExample(code = NOT_FOUND_USER, name = "회원 없음", message = "회원이 존재하지 않습니다.", description = "탈퇴하려는 회원이 존재하지 않는 경우"),
            ExceptionExample(code = INTERNAL_SERVER_ERROR, name = "서버 오류", message = "서버 에러입니다. 개발자에게 문의해주세요.", description = "예상치 못한 서버 오류가 발생한 경우"),
        ]
    )
    fun withdraw(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @RequestHeader(name = ACCESS_TOKEN_HEADER_NAME, required = false) accessToken: String?,
    ): ResponseEntity<Void>

    companion object {
        const val ACCESS_TOKEN_HEADER_NAME = "Authorization"
    }
}
