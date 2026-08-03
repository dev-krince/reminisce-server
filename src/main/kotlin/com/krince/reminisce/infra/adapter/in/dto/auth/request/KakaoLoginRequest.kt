package com.krince.reminisce.infra.adapter.`in`.dto.auth.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(title = "KakaoLoginRequest", description = "카카오 소셜 로그인 요청")
class KakaoLoginRequest(
    @field:Schema(description = "카카오 인가코드", example = "authorization-code-from-kakao", required = true)
    @field:NotBlank(message = "인가코드는 비어있을 수 없습니다.")
    val authorizationCode: String,
)
