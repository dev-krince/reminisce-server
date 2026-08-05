package com.krince.reminisce.infra.adapter.`in`.dto.speakingsession.response

import com.fasterxml.jackson.annotation.JsonFormat
import com.krince.reminisce.application.port.`in`.speakingsession.result.SpeakingSessionResult
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(title = "SpeakingSessionResponse", description = "말하기 세션 응답")
class SpeakingSessionResponse(
    @field:Schema(description = "말하기 세션 고유 식별자", example = "01920000-0000-7000-8000-000000000010", required = true)
    val sessionId: String,

    @field:Schema(description = "아이 고유 식별자", example = "e443e5c3-0243-4d28-ba79-37cf3b923023", required = true)
    val childId: String,

    @field:Schema(description = "이야기 고유 식별자", example = "01920000-0000-7000-8000-000000000001", required = true)
    val storyId: String,

    @field:Schema(description = "세션 상태", example = "IN_PROGRESS", required = true)
    val status: String,

    @field:Schema(description = "현재 장면 식별자", example = "null", required = false)
    val currentSceneId: String?,

    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @field:Schema(description = "세션 시작 시각", example = "2026-01-09 14:30:25", required = true)
    val startedAt: LocalDateTime,
)

fun speakingSessionResponse(result: SpeakingSessionResult): SpeakingSessionResponse = SpeakingSessionResponse(
    sessionId = result.sessionId,
    childId = result.childId,
    storyId = result.storyId,
    status = result.status,
    currentSceneId = result.currentSceneId,
    startedAt = result.startedAt,
)
