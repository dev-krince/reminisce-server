package com.krince.reminisce.infra.adapter.`in`.dto.speakingsession.response

import com.fasterxml.jackson.annotation.JsonFormat
import com.krince.reminisce.application.port.`in`.speakingsession.result.SpeakingSessionSummaryResult
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(title = "SpeakingSessionSummaryResponse", description = "이어하기 세션 요약 응답")
class SpeakingSessionSummaryResponse(
    @field:Schema(description = "말하기 세션 고유 식별자", example = "01920000-0000-7000-8000-000000000010", required = true)
    val sessionId: String,

    @field:Schema(description = "이야기 고유 식별자", example = "01920000-0000-7000-8000-000000000001", required = true)
    val storyId: String,

    @field:Schema(description = "세션 상태", example = "IN_PROGRESS", required = true)
    val status: String,

    @field:Schema(description = "현재 장면 식별자", example = "null", required = false)
    val currentSceneId: String?,

    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @field:Schema(description = "세션 시작 시각", example = "2026-01-09 14:30:25", required = true)
    val startedAt: LocalDateTime,

    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @field:Schema(description = "마지막 활동 시각", example = "2026-01-09 15:00:00", required = true)
    val lastActivityAt: LocalDateTime,
)

fun speakingSessionSummaryResponse(result: SpeakingSessionSummaryResult): SpeakingSessionSummaryResponse =
    SpeakingSessionSummaryResponse(
        sessionId = result.sessionId,
        storyId = result.storyId,
        status = result.status,
        currentSceneId = result.currentSceneId,
        startedAt = result.startedAt,
        lastActivityAt = result.lastActivityAt,
    )

fun speakingSessionSummaryResponses(results: List<SpeakingSessionSummaryResult>): List<SpeakingSessionSummaryResponse> =
    results.map { speakingSessionSummaryResponse(it) }
