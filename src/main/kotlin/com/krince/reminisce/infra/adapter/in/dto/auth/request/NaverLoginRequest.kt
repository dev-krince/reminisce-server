package com.krince.reminisce.infra.adapter.`in`.dto.auth.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(title = "NaverLoginRequest", description = "네이버 소셜 로그인 요청")
class NaverLoginRequest(
    @field:Schema(description = "네이버 인가코드", example = "authorization-code-from-naver", required = true)
    @field:NotBlank(message = "인가코드는 비어있을 수 없습니다.")
    val authorizationCode: String,

    @field:Schema(description = "네이버 state 값", example = "state-value-from-naver", required = true)
    @field:NotBlank(message = "state는 비어있을 수 없습니다.")
    val state: String,
)
