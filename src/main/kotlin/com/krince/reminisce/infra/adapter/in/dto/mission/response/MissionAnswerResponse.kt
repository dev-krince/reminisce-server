package com.krince.reminisce.infra.adapter.`in`.dto.mission.response

import com.krince.reminisce.application.port.`in`.mission.result.MissionAnswerResult
import io.swagger.v3.oas.annotations.media.Schema

@Schema(title = "MissionAnswerResponse", description = "미션 답안 제출 결과 응답")
data class MissionAnswerResponse(
    @field:Schema(description = "미션 완료 여부", required = true)
    val completed: Boolean,

    @field:Schema(description = "시도 횟수", required = true)
    val attemptCount: Int,

    @field:Schema(description = "미완료 시 안내 힌트 목록", required = true)
    val hints: List<String>,
)

fun missionAnswerResponse(result: MissionAnswerResult): MissionAnswerResponse = MissionAnswerResponse(
    completed = result.completed,
    attemptCount = result.attemptCount,
    hints = result.hints,
)
