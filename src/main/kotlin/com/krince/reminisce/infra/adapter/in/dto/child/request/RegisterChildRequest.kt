package com.krince.reminisce.infra.adapter.`in`.dto.child.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(title = "RegisterChildRequest", description = "아이 프로필 등록 요청")
class RegisterChildRequest(
    @field:Schema(description = "아이 애칭", example = "토토", required = true)
    @field:NotBlank(message = "애칭은 비어있을 수 없습니다.")
    val nickname: String,

    @field:Schema(description = "출생연도", example = "2019", required = true)
    val birthYear: Int,

    @field:Schema(description = "동의서 버전", example = "v1.0", required = true)
    @field:NotBlank(message = "동의서 버전은 비어있을 수 없습니다.")
    val consentVersion: String,
)
