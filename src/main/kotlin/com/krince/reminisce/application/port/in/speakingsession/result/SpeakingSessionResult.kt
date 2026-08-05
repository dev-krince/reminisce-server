package com.krince.reminisce.application.port.`in`.speakingsession.result

import com.krince.reminisce.domain.model.speakingsession.SpeakingSession
import java.time.LocalDateTime

class SpeakingSessionResult(
    val sessionId: String,
    val childId: String,
    val storyId: String,
    val status: String,
    val currentSceneId: String?,
    val startedAt: LocalDateTime,
    val created: Boolean,
) {
    companion object {
        fun from(session: SpeakingSession, created: Boolean): SpeakingSessionResult = SpeakingSessionResult(
            sessionId = session.sessionId.value,
            childId = session.childId.value,
            storyId = session.storyId.value,
            status = session.status.name,
            currentSceneId = session.currentSceneId,
            startedAt = session.startedAt,
            created = created,
        )
    }
}
