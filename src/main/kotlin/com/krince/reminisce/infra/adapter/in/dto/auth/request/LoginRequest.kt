package com.krince.reminisce.infra.adapter.`in`.dto.auth.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

@Schema(title = "LoginRequest", description = "이메일 로그인 요청")
class LoginRequest(
    @field:Schema(description = "이메일", example = "user@example.com", required = true)
    @field:NotBlank(message = "이메일은 비어있을 수 없습니다.")
    @field:Email(message = "올바르지 않은 이메일 형식입니다.")
    val email: String,

    @field:Schema(description = "비밀번호", example = "Password1!", required = true)
    @field:NotBlank(message = "비밀번호는 비어있을 수 없습니다.")
    val password: String,
)
