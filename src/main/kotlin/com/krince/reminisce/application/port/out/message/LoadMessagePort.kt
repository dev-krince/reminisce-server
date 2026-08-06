package com.krince.reminisce.application.port.out.message

import com.krince.reminisce.domain.model.speakingsession.vo.SpeakingSessionId

interface LoadMessagePort {
    fun countBySession(sessionId: SpeakingSessionId): Long
}
