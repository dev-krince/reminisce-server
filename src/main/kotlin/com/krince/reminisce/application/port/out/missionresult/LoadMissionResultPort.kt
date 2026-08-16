package com.krince.reminisce.application.port.out.missionresult

import com.krince.reminisce.domain.model.missionresult.MissionResult
import com.krince.reminisce.domain.model.speakingsession.vo.SpeakingSessionId

interface LoadMissionResultPort {
    fun findBySessionAndScene(sessionId: SpeakingSessionId, sceneId: String): MissionResult?
}
