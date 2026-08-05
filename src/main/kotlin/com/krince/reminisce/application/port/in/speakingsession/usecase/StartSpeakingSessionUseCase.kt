package com.krince.reminisce.application.port.`in`.speakingsession.usecase

import com.krince.reminisce.application.port.`in`.speakingsession.command.StartSpeakingSessionCommand
import com.krince.reminisce.application.port.`in`.speakingsession.result.SpeakingSessionResult

interface StartSpeakingSessionUseCase {
    fun execute(command: StartSpeakingSessionCommand): SpeakingSessionResult
}
