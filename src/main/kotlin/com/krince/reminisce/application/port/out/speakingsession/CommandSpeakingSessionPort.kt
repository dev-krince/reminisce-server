package com.krince.reminisce.application.port.out.speakingsession

import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.speakingsession.SpeakingSession
import com.krince.reminisce.domain.model.speakingsession.vo.SpeakingSessionId

interface CommandSpeakingSessionPort {
    fun save(session: SpeakingSession): SpeakingSession

    fun deleteById(sessionId: SpeakingSessionId)

    fun deleteAllByChildIds(childIds: List<ChildId>)
}
