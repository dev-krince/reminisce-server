package com.krince.reminisce.application.port.out.speakingsession

import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.speakingsession.SpeakingSession
import com.krince.reminisce.domain.model.speakingsession.vo.SpeakingSessionId
import com.krince.reminisce.domain.model.story.vo.StoryId

interface LoadSpeakingSessionPort {
    fun findInProgress(childId: ChildId, storyId: StoryId): SpeakingSession?

    fun findById(sessionId: SpeakingSessionId): SpeakingSession?

    fun findResumableByChild(childId: ChildId): List<SpeakingSession>

    fun findLatestCompletedByChild(childId: ChildId): SpeakingSession?

    fun findStartedStoryIdsByChild(childId: ChildId): List<String>

    fun findSessionIdsByChildIds(childIds: List<ChildId>): List<String>
}
