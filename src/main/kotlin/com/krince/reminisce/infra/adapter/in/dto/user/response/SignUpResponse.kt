package com.krince.reminisce.infra.adapter.`in`.dto.user.response

import com.krince.reminisce.application.port.access.user.context.UserResult
import io.swagger.v3.oas.annotations.media.Schema

@Schema(title = "SignUpResponse", description = "회원가입 응답")
class SignUpResponse(
    @field:Schema(description = "회원 고유 식별자", example = "e443e5c3-0243-4d28-ba79-37cf3b923023", required = true)
    val id: String,

    @field:Schema(description = "이메일", example = "user@example.com", required = true)
    val email: String,

    @field:Schema(description = "닉네임", example = "홍길동", required = true)
    val nickname: String,

    @field:Schema(description = "회원 권한", example = "ROLE_USER", required = true)
    val role: String,
)

fun signUpResponse(userResult: UserResult): SignUpResponse = SignUpResponse(
    id = userResult.userId,
    email = userResult.email,
    nickname = userResult.nickname,
    role = userResult.role,
)
