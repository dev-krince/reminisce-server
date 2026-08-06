package com.krince.reminisce.application.port.`in`.speakingsession.usecase

import com.krince.reminisce.application.port.`in`.speakingsession.command.GetResumableSessionsCommand
import com.krince.reminisce.application.port.`in`.speakingsession.result.SpeakingSessionSummaryResult

interface GetResumableSessionsUseCase {
    fun execute(command: GetResumableSessionsCommand): List<SpeakingSessionSummaryResult>
}
