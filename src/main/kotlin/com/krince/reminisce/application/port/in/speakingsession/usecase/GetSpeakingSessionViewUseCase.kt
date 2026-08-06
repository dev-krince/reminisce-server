package com.krince.reminisce.application.port.`in`.speakingsession.usecase

import com.krince.reminisce.application.port.`in`.speakingsession.command.GetSpeakingSessionViewCommand
import com.krince.reminisce.application.port.`in`.speakingsession.result.SpeakingSessionViewResult

interface GetSpeakingSessionViewUseCase {
    fun execute(command: GetSpeakingSessionViewCommand): SpeakingSessionViewResult
}
