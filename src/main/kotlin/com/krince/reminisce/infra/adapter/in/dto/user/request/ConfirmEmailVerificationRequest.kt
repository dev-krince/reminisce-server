package com.krince.reminisce.infra.adapter.`in`.dto.user.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

@Schema(title = "ConfirmEmailVerificationRequest", description = "이메일 인증코드 확인 요청")
class ConfirmEmailVerificationRequest(
    @field:Schema(description = "이메일", example = "user@example.com", required = true)
    @field:NotBlank(message = "이메일은 비어있을 수 없습니다.")
    @field:Email(message = "올바르지 않은 이메일 형식입니다.")
    val email: String,

    @field:Schema(description = "인증코드", example = "123456", required = true)
    @field:NotBlank(message = "인증코드는 비어있을 수 없습니다.")
    val code: String,
)
