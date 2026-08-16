package com.krince.reminisce.application.port.`in`.speakingsession.result

import com.krince.reminisce.application.port.access.story.ResumableStoryDisplayInfo
import com.krince.reminisce.domain.model.speakingsession.SpeakingSession
import java.time.LocalDateTime

class SpeakingSessionSummaryResult(
    val sessionId: String,
    val storyId: String,
    val status: String,
    val currentSceneId: String?,
    val startedAt: LocalDateTime,
    val lastActivityAt: LocalDateTime,
    val title: String,
    val representativeImageUrl: String?,
    val difficulty: String,
    val topics: List<String>,
    val currentChapter: Int,
    val totalChapters: Int,
) {
    companion object {
        fun from(
            session: SpeakingSession,
            displayInfo: ResumableStoryDisplayInfo,
        ): SpeakingSessionSummaryResult = SpeakingSessionSummaryResult(
            sessionId = session.sessionId.value,
            storyId = session.storyId.value,
            status = session.status.name,
            currentSceneId = session.currentSceneId,
            startedAt = session.startedAt,
            lastActivityAt = session.lastActivityAt,
            title = displayInfo.title,
            representativeImageUrl = displayInfo.representativeImageUrl,
            difficulty = displayInfo.difficulty,
            topics = displayInfo.topics,
            currentChapter = displayInfo.currentChapter,
            totalChapters = displayInfo.totalChapters,
        )
    }
}
