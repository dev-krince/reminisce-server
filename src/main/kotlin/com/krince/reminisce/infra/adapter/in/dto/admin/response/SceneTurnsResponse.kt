package com.krince.reminisce.infra.adapter.`in`.dto.admin.response

import com.krince.reminisce.application.port.`in`.admin.result.SceneTurnsResult
import io.swagger.v3.oas.annotations.media.Schema

@Schema(title = "SceneTurnsResponse", description = "장면 발화 횟수 설정 결과")
class SceneTurnsResponse(
    @field:Schema(description = "장면 고유 식별자", example = "sc_banggui_04", required = true)
    val sceneId: String,

    @field:Schema(description = "최소 발화(preferredTurns)", example = "2", required = false)
    val preferredTurns: Int?,

    @field:Schema(description = "최대 발화(maxTurns)", example = "4", required = false)
    val maxTurns: Int?,
)

fun sceneTurnsResponse(result: SceneTurnsResult): SceneTurnsResponse = SceneTurnsResponse(
    sceneId = result.sceneId,
    preferredTurns = result.preferredTurns,
    maxTurns = result.maxTurns,
)
