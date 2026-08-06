package com.krince.reminisce.application.port.out.speakingsession

import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.speakingsession.SpeakingSession

interface CommandSpeakingSessionPort {
    fun save(session: SpeakingSession): SpeakingSession

    fun deleteAllByChildIds(childIds: List<ChildId>)
}
