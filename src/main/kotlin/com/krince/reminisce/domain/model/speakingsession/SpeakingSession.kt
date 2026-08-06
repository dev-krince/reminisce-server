package com.krince.reminisce.domain.model.speakingsession

import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.speakingsession.vo.SessionStatus
import com.krince.reminisce.domain.model.speakingsession.vo.SpeakingSessionId
import com.krince.reminisce.domain.model.story.vo.StoryId
import com.krince.reminisce.shared.util.UuidGenerator
import java.time.LocalDateTime

class SpeakingSession(
    val sessionId: SpeakingSessionId,
    val childId: ChildId,
    val storyId: StoryId,
    val status: SessionStatus,
    val currentSceneId: String? = null,
    val startedAt: LocalDateTime,
    val lastActivityAt: LocalDateTime,
    val createdDate: LocalDateTime? = null,
    val modifiedDate: LocalDateTime? = null,
) {
    fun advanceToScene(sceneId: String, at: LocalDateTime): SpeakingSession = SpeakingSession(
        sessionId = sessionId,
        childId = childId,
        storyId = storyId,
        status = status,
        currentSceneId = sceneId,
        startedAt = startedAt,
        lastActivityAt = at,
        createdDate = createdDate,
        modifiedDate = modifiedDate,
    )

    companion object {
        fun start(childId: ChildId, storyId: StoryId, at: LocalDateTime): SpeakingSession = SpeakingSession(
            sessionId = SpeakingSessionId(UuidGenerator.generate()),
            childId = childId,
            storyId = storyId,
            status = SessionStatus.IN_PROGRESS,
            currentSceneId = null,
            startedAt = at,
            lastActivityAt = at,
        )
    }
}
