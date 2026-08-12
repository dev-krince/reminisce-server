package com.krince.reminisce.application.service.child

import com.krince.reminisce.application.port.out.message.CommandMessagePort
import com.krince.reminisce.application.port.out.message.LoadMessagePort
import com.krince.reminisce.application.port.out.postactivityresult.CommandPostActivityResultPort
import com.krince.reminisce.application.port.out.postactivityresult.LoadPostActivityResultPort
import com.krince.reminisce.application.port.out.report.CommandReportPort
import com.krince.reminisce.application.port.out.speakingsession.CommandSpeakingSessionPort
import com.krince.reminisce.application.port.out.speakingsession.LoadSpeakingSessionPort
import com.krince.reminisce.application.port.out.utteranceanalysis.CommandUtteranceAnalysisPort
import com.krince.reminisce.application.port.out.wordbook.CommandSavedWordPort
import com.krince.reminisce.domain.model.child.vo.ChildId
import org.springframework.stereotype.Component

@Component
class ChildLearningDataPurger(
    private val loadSpeakingSessionPort: LoadSpeakingSessionPort,
    private val loadMessagePort: LoadMessagePort,
    private val loadPostActivityResultPort: LoadPostActivityResultPort,
    private val commandSpeakingSessionPort: CommandSpeakingSessionPort,
    private val commandMessagePort: CommandMessagePort,
    private val commandReportPort: CommandReportPort,
    private val commandPostActivityResultPort: CommandPostActivityResultPort,
    private val commandUtteranceAnalysisPort: CommandUtteranceAnalysisPort,
    private val commandSavedWordPort: CommandSavedWordPort,
) {

    fun purge(childIds: List<ChildId>): List<String> {
        if (childIds.isEmpty()) {
            return emptyList()
        }
        val retellingAudioUrls: List<String> = purgeSessionData(childIds)
        commandSavedWordPort.deleteAllByChildIds(childIds)

        return retellingAudioUrls
    }

    private fun purgeSessionData(childIds: List<ChildId>): List<String> {
        val sessionIds: List<String> = loadSpeakingSessionPort.findSessionIdsByChildIds(childIds)
        if (sessionIds.isEmpty()) {
            return emptyList()
        }
        val retellingAudioUrls: List<String> = loadPostActivityResultPort.findRetellingAudioUrlsBySessionIds(sessionIds)
        val messageIds: List<String> = loadMessagePort.findMessageIdsBySessionIds(sessionIds)
        if (messageIds.isNotEmpty()) {
            commandUtteranceAnalysisPort.deleteAllByMessageIds(messageIds)
        }
        commandMessagePort.deleteAllBySessionIds(sessionIds)
        commandReportPort.deleteAllBySessionIds(sessionIds)
        commandPostActivityResultPort.deleteAllBySessionIds(sessionIds)
        commandSpeakingSessionPort.deleteAllByChildIds(childIds)

        return retellingAudioUrls
    }
}
