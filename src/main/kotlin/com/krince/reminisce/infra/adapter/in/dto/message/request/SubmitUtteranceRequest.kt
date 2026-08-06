package com.krince.reminisce.infra.adapter.`in`.dto.message.request

import io.swagger.v3.oas.annotations.media.Schema

@Schema(title = "SubmitUtteranceRequest", description = "아이 발화 제출 요청")
class SubmitUtteranceRequest(
    @field:Schema(description = "아이 발화 오디오(스텁 단계에서는 텍스트로 대체)", example = "며느리가 참 힘들었겠어요", required = true)
    val audio: String,
)
