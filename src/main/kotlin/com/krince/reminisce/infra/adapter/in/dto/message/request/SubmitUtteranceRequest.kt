package com.krince.reminisce.infra.adapter.`in`.dto.message.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(title = "SubmitUtteranceRequest", description = "아이 발화 제출 요청")
class SubmitUtteranceRequest(
    @field:NotBlank(message = "발화 텍스트는 비어있을 수 없습니다.")
    @field:Schema(description = "기기 STT로 확정한 아이 발화 텍스트", example = "며느리가 참 힘들었겠어요", required = true)
    val text: String,
    @field:Schema(description = "기기 STT 최초 변환 원본 텍스트(선택)", example = "며느리가 참 힘들었겠어요", required = false)
    val sttRawText: String? = null,
)
