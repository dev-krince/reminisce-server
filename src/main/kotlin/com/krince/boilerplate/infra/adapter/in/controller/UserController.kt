package com.krince.boilerplate.infra.adapter.`in`.controller

import com.krince.boilerplate.infra.adapter.`in`.dto.user.response.UserResponse
import com.krince.boilerplate.infra.swagger.ExceptionExample
import com.krince.boilerplate.infra.swagger.SwaggerExceptionResponse
import com.krince.boilerplate.infra.swagger.SwaggerSuccessResponse
import com.krince.boilerplate.shared.response.ExceptionResponseCode.*
import com.krince.boilerplate.shared.response.SuccessResponse
import com.krince.boilerplate.shared.response.SuccessResponseCode.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.NotBlank
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable

@Tag(name = "회원(Users)")
interface UserController {

    //[GET] /api/users/{userId}
    @Operation(summary = "회원 단건 조회", description = "회원 고유 식별자로 특정 회원의 상세 정보를 조회합니다.")
    @SwaggerSuccessResponse(responseCode = OK, description = "회원 조회 성공")
    @SwaggerExceptionResponse(
        examples = [
            ExceptionExample(code = INVALID_DTO_PARAMETER, name = "유효하지 않은 회원 ID", message = "회원 ID는 비어있을 수 없습니다.", description = "회원 ID가 비어있는 경우"),
            ExceptionExample(code = EMPTY_TOKEN, name = "토큰 없음", message = "토큰이 없습니다.", description = "인증 토큰이 제공되지 않은 경우"),
            ExceptionExample(code = INVALID_TOKEN, name = "유효하지 않은 토큰", message = "유효하지 않은 토큰입니다.", description = "토큰이 유효하지 않거나 서명이 잘못된 경우"),
            ExceptionExample(code = EXPIRED_TOKEN, name = "만료된 토큰", message = "만료된 토큰입니다.", description = "토큰의 유효기간이 만료된 경우"),
            ExceptionExample(code = NOT_FOUND_USER, name = "회원 없음", message = "회원이 존재하지 않습니다.", description = "조회하려는 회원이 존재하지 않는 경우"),
            ExceptionExample(code = INTERNAL_SERVER_ERROR, name = "서버 오류", message = "서버 에러입니다. 개발자에게 문의해주세요.", description = "예상치 못한 서버 오류가 발생한 경우"),
        ]
    )
    fun getUser(
        @Parameter(description = "회원 고유 식별자", example = "e443e5c3-0243-4d28-ba79-37cf3b923023", required = true)
        @NotBlank(message = "회원 식별자는 비어있을 수 없습니다.")
        @PathVariable
        userId: String,
    ): ResponseEntity<SuccessResponse<UserResponse>>
}