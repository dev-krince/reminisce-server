package com.krince.reminisce.application.facade.speakingsession

import com.krince.reminisce.application.port.access.speakingsession.SpeakingSessionAccessPort
import com.krince.reminisce.application.port.out.speakingsession.LoadSpeakingSessionPort
import com.krince.reminisce.domain.model.child.vo.ChildId
import org.springframework.stereotype.Service

@Service
class SpeakingSessionAccessFacade(
    private val loadSpeakingSessionPort: LoadSpeakingSessionPort,
) : SpeakingSessionAccessPort {

    override fun findStartedStoryIds(childId: ChildId): List<String> =
        loadSpeakingSessionPort.findStartedStoryIdsByChild(childId)
}
