package com.krince.reminisce.application.service.speakingsession

import com.krince.reminisce.application.port.out.message.CommandMessagePort
import com.krince.reminisce.application.port.out.message.LoadMessagePort
import com.krince.reminisce.application.port.out.missionresult.CommandMissionResultPort
import com.krince.reminisce.application.port.out.postactivityresult.CommandPostActivityResultPort
import com.krince.reminisce.application.port.out.postactivityresult.LoadPostActivityResultPort
import com.krince.reminisce.application.port.out.report.CommandReportPort
import com.krince.reminisce.application.port.out.utteranceanalysis.CommandUtteranceAnalysisPort
import org.springframework.stereotype.Component

@Component
class SpeakingSessionCascadePurger(
    private val loadMessagePort: LoadMessagePort,
    private val loadPostActivityResultPort: LoadPostActivityResultPort,
    private val commandMessagePort: CommandMessagePort,
    private val commandReportPort: CommandReportPort,
    private val commandPostActivityResultPort: CommandPostActivityResultPort,
    private val commandMissionResultPort: CommandMissionResultPort,
    private val commandUtteranceAnalysisPort: CommandUtteranceAnalysisPort,
) {

    fun purgeBySessionIds(sessionIds: List<String>): List<String> {
        if (sessionIds.isEmpty()) {
            return emptyList()
        }
        val retellingAudioUrls: List<String> = loadPostActivityResultPort.findRetellingAudioUrlsBySessionIds(sessionIds)
        val utteranceAudioUrls: List<String> = loadMessagePort.findAudioUrlsBySessionIds(sessionIds)
        val messageIds: List<String> = loadMessagePort.findMessageIdsBySessionIds(sessionIds)
        if (messageIds.isNotEmpty()) {
            commandUtteranceAnalysisPort.deleteAllByMessageIds(messageIds)
        }
        commandMessagePort.deleteAllBySessionIds(sessionIds)
        commandReportPort.deleteAllBySessionIds(sessionIds)
        commandPostActivityResultPort.deleteAllBySessionIds(sessionIds)
        commandMissionResultPort.deleteAllBySessionIds(sessionIds)

        return retellingAudioUrls + utteranceAudioUrls
    }
}
