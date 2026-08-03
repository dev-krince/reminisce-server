package com.krince.reminisce.infra.adapter.`in`.dto.user.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

@Schema(title = "SignUpRequest", description = "이메일 회원가입 요청")
class SignUpRequest(
    @field:Schema(description = "이메일", example = "user@example.com", required = true)
    @field:NotBlank(message = "이메일은 비어있을 수 없습니다.")
    @field:Email(message = "올바르지 않은 이메일 형식입니다.")
    val email: String,

    @field:Schema(description = "비밀번호 (8자 이상, 영문/숫자/특수문자 포함)", example = "Password1!", required = true)
    @field:NotBlank(message = "비밀번호는 비어있을 수 없습니다.")
    val password: String,

    @field:Schema(description = "닉네임", example = "홍길동", required = true)
    @field:NotBlank(message = "닉네임은 비어있을 수 없습니다.")
    val nickname: String,
)
