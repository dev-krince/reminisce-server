package com.krince.reminisce.infra.adapter.`in`.dto.mission.request

import io.swagger.v3.oas.annotations.media.Schema

@Schema(title = "SubmitMissionAnswerRequest", description = "미션 답안 제출 요청")
data class SubmitMissionAnswerRequest(
    @field:Schema(description = "제출한 단어카드 순서 목록 (WORD_ORDER 전용)", required = false)
    val submittedOrder: List<String>? = null,

    @field:Schema(description = "발화 텍스트 (SPEAKING 전용)", required = false)
    val text: String? = null,
)
