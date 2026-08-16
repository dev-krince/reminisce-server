package com.krince.reminisce.infra.adapter.`in`.dto.profileinterview.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(title = "StartProfileInterviewRequest", description = "프로필 인터뷰 시작 요청")
class StartProfileInterviewRequest(
    @field:Schema(description = "인터뷰 대상 아이 고유 식별자", example = "e443e5c3-0243-4d28-ba79-37cf3b923023", required = true)
    @field:NotBlank(message = "아이 식별자는 비어있을 수 없습니다.")
    val childId: String,
)
