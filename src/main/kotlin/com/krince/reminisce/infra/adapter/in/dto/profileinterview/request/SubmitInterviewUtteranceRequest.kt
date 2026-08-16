package com.krince.reminisce.infra.adapter.`in`.dto.profileinterview.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(title = "SubmitInterviewUtteranceRequest", description = "프로필 인터뷰 아이 발화 제출 요청")
class SubmitInterviewUtteranceRequest(
    @field:Schema(description = "확정된 아이 발화 텍스트", example = "토끼요.", required = true)
    @field:NotBlank(message = "발화 텍스트는 비어있을 수 없습니다.")
    val text: String,

    @field:Schema(description = "STT 원문 (선택)", example = "토끼요", required = false)
    val sttRawText: String? = null,
)
