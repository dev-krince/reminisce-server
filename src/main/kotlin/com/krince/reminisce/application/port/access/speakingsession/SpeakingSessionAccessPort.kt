package com.krince.reminisce.application.port.access.speakingsession

import com.krince.reminisce.domain.model.child.vo.ChildId

interface SpeakingSessionAccessPort {
    fun findStartedStoryIds(childId: ChildId): List<String>
}
