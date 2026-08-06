package com.krince.reminisce.infra.adapter.`in`.dto.auth.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(title = "GoogleLoginRequest", description = "구글 소셜 로그인 요청")
class GoogleLoginRequest(
    @field:Schema(description = "구글 인가코드", example = "authorization-code-from-google", required = true)
    @field:NotBlank(message = "인가코드는 비어있을 수 없습니다.")
    val authorizationCode: String,
)
