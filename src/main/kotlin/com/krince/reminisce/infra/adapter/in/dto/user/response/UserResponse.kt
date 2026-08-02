package com.krince.reminisce.infra.adapter.`in`.dto.user.response

import com.fasterxml.jackson.annotation.JsonFormat
import com.krince.reminisce.application.port.access.user.context.UserResult
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(title = "UserResponse", description = "회원 정보 응답")
class UserResponse(
    @field:Schema(description = "회원 고유 식별자", example = "e443e5c3-0243-4d28-ba79-37cf3b923023", required = true)
    val id: String,

    @field:Schema(description = "로그인 아이디", example = "testUser", required = true)
    val loginId: String,

    @field:Schema(description = "회원 권한", example = "ROLE_USER", required = true)
    val role: String,

    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @field:Schema(description = "생성일시", example = "2026-01-09 14:30:25", required = true)
    val createdDate: LocalDateTime,

    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @field:Schema(description = "수정일시", example = "2026-01-09 14:30:25", required = true)
    val modifiedDate: LocalDateTime
)

fun userResponse(userResult: UserResult): UserResponse = UserResponse(
    id = userResult.userId,
    loginId = userResult.loginId,
    role = userResult.role,
    createdDate = userResult.createdDate,
    modifiedDate = userResult.modifiedDate,
)