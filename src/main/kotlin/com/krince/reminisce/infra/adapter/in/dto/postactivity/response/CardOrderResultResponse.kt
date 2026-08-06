package com.krince.reminisce.infra.adapter.`in`.dto.postactivity.response

import com.fasterxml.jackson.annotation.JsonProperty
import com.krince.reminisce.application.port.`in`.postactivity.result.CardOrderResult
import io.swagger.v3.oas.annotations.media.Schema

@Schema(title = "CardOrderResultResponse", description = "카드 순서 제출 결과 응답")
data class CardOrderResultResponse(
    @field:Schema(description = "정답 여부", required = true)
    @field:JsonProperty("isOrderCorrect")
    val isOrderCorrect: Boolean,

    @field:Schema(description = "시도 횟수", required = true)
    val attemptCount: Int,

    @field:Schema(description = "재구성 핵심 단어 목록 (정답일 때만 포함)", required = false)
    val retellingKeywords: List<String>?,
)

fun cardOrderResultResponse(result: CardOrderResult): CardOrderResultResponse = CardOrderResultResponse(
    isOrderCorrect = result.isOrderCorrect,
    attemptCount = result.attemptCount,
    retellingKeywords = if (result.retellingKeywords.isEmpty()) null else result.retellingKeywords,
)
