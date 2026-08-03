package com.krince.reminisce.infra.adapter.`in`.dto.user.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

@Schema(title = "SendEmailVerificationRequest", description = "이메일 인증코드 발송 요청")
class SendEmailVerificationRequest(
    @field:Schema(description = "이메일", example = "user@example.com", required = true)
    @field:NotBlank(message = "이메일은 비어있을 수 없습니다.")
    @field:Email(message = "올바르지 않은 이메일 형식입니다.")
    val email: String,
)
