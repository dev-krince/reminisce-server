package com.krince.reminisce.application.port.`in`.speakingsession.usecase

import com.krince.reminisce.application.port.`in`.speakingsession.command.StopSpeakingSessionCommand
import com.krince.reminisce.application.port.`in`.speakingsession.result.SpeakingSessionResult

interface StopSpeakingSessionUseCase {
    fun execute(command: StopSpeakingSessionCommand): SpeakingSessionResult
}
