package com.krince.reminisce.infra.adapter.`in`.dto.speakingsession.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(title = "StartSpeakingSessionRequest", description = "말하기 세션 시작 요청")
class StartSpeakingSessionRequest(
    @field:Schema(description = "아이 고유 식별자", example = "e443e5c3-0243-4d28-ba79-37cf3b923023", required = true)
    @field:NotBlank(message = "아이 식별자는 비어있을 수 없습니다.")
    val childId: String,

    @field:Schema(description = "이야기 고유 식별자", example = "01920000-0000-7000-8000-000000000001", required = true)
    @field:NotBlank(message = "이야기 식별자는 비어있을 수 없습니다.")
    val storyId: String,
)
