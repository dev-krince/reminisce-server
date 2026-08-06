package com.krince.reminisce.application.port.`in`.speakingsession.result

import com.krince.reminisce.domain.model.speakingsession.SpeakingSession
import java.time.LocalDateTime

class SpeakingSessionSummaryResult(
    val sessionId: String,
    val storyId: String,
    val status: String,
    val currentSceneId: String?,
    val startedAt: LocalDateTime,
    val lastActivityAt: LocalDateTime,
) {
    companion object {
        fun from(session: SpeakingSession): SpeakingSessionSummaryResult = SpeakingSessionSummaryResult(
            sessionId = session.sessionId.value,
            storyId = session.storyId.value,
            status = session.status.name,
            currentSceneId = session.currentSceneId,
            startedAt = session.startedAt,
            lastActivityAt = session.lastActivityAt,
        )
    }
}
