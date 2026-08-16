package com.krince.reminisce.application.service.child

import com.krince.reminisce.application.port.out.profileinterview.CommandInterviewMessagePort
import com.krince.reminisce.application.port.out.profileinterview.CommandProfileInterviewPort
import com.krince.reminisce.application.port.out.profileinterview.LoadProfileInterviewPort
import com.krince.reminisce.application.port.out.savedstory.CommandSavedStoryPort
import com.krince.reminisce.application.port.out.speakingsession.CommandSpeakingSessionPort
import com.krince.reminisce.application.port.out.speakingsession.LoadSpeakingSessionPort
import com.krince.reminisce.application.port.out.storyprofile.CommandStoryProfilePort
import com.krince.reminisce.application.port.out.wordbook.CommandSavedWordPort
import com.krince.reminisce.application.service.speakingsession.SpeakingSessionCascadePurger
import com.krince.reminisce.domain.model.child.vo.ChildId
import org.springframework.stereotype.Component

@Component
class ChildLearningDataPurger(
    private val loadSpeakingSessionPort: LoadSpeakingSessionPort,
    private val commandSpeakingSessionPort: CommandSpeakingSessionPort,
    private val sessionCascadePurger: SpeakingSessionCascadePurger,
    private val commandSavedWordPort: CommandSavedWordPort,
    private val commandSavedStoryPort: CommandSavedStoryPort,
    private val loadProfileInterviewPort: LoadProfileInterviewPort,
    private val commandProfileInterviewPort: CommandProfileInterviewPort,
    private val commandInterviewMessagePort: CommandInterviewMessagePort,
    private val commandStoryProfilePort: CommandStoryProfilePort,
) {

    fun purge(childIds: List<ChildId>): List<String> {
        if (childIds.isEmpty()) {
            return emptyList()
        }
        val audioUrls: List<String> = purgeSessionData(childIds)
        purgeProfileInterviewData(childIds)
        commandSavedWordPort.deleteAllByChildIds(childIds)
        commandSavedStoryPort.deleteAllByChildIds(childIds)

        return audioUrls
    }

    private fun purgeSessionData(childIds: List<ChildId>): List<String> {
        val sessionIds: List<String> = loadSpeakingSessionPort.findSessionIdsByChildIds(childIds)
        if (sessionIds.isEmpty()) {
            return emptyList()
        }
        val audioUrls: List<String> = sessionCascadePurger.purgeBySessionIds(sessionIds)
        commandSpeakingSessionPort.deleteAllByChildIds(childIds)

        return audioUrls
    }

    private fun purgeProfileInterviewData(childIds: List<ChildId>) {
        val interviewIds: List<String> = loadProfileInterviewPort.findInterviewIdsByChildIds(childIds)
        if (interviewIds.isNotEmpty()) {
            commandInterviewMessagePort.deleteAllByInterviewIds(interviewIds)
        }
        commandStoryProfilePort.deleteAllByChildIds(childIds)
        commandProfileInterviewPort.deleteAllByChildIds(childIds)
    }
}
