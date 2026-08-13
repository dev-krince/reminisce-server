package com.krince.reminisce.infra.adapter.`in`.dto.speakingsession.response

import com.krince.reminisce.application.port.`in`.speakingsession.result.SpeakingHintResult
import io.swagger.v3.oas.annotations.media.Schema

@Schema(title = "SpeakingHintResponse", description = "말하기 힌트 응답")
class SpeakingHintResponse(
    @field:Schema(description = "현재 장면 미션 목표", example = "며느리의 마음을 헤아려 말해요", required = false)
    val goal: String?,

    @field:Schema(description = "아이에게 보여줄 예시 힌트 목록", example = "[\"며느리가 왜 힘든지 말해볼까요?\"]", required = true)
    val hints: List<String>,
)

fun speakingHintResponse(result: SpeakingHintResult): SpeakingHintResponse =
    SpeakingHintResponse(
        goal = result.goal,
        hints = result.hints,
    )
