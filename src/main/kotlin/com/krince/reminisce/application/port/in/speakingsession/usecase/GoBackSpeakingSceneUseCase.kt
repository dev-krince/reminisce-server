package com.krince.reminisce.application.port.`in`.speakingsession.usecase

import com.krince.reminisce.application.port.`in`.speakingsession.command.GoBackSpeakingSceneCommand
import com.krince.reminisce.application.port.`in`.speakingsession.result.SpeakingSessionViewResult

interface GoBackSpeakingSceneUseCase {
    fun execute(command: GoBackSpeakingSceneCommand): SpeakingSessionViewResult
}
