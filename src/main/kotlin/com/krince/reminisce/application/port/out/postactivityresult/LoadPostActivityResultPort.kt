package com.krince.reminisce.application.port.out.postactivityresult

import com.krince.reminisce.domain.model.postactivityresult.PostActivityResult
import com.krince.reminisce.domain.model.speakingsession.vo.SpeakingSessionId

interface LoadPostActivityResultPort {
    fun findBySession(sessionId: SpeakingSessionId): PostActivityResult?
}
