package com.krince.reminisce.application.port.`in`.speakingsession.usecase

import com.krince.reminisce.application.port.`in`.speakingsession.command.AdvanceSpeakingSceneCommand
import com.krince.reminisce.application.port.`in`.speakingsession.result.SpeakingSessionViewResult

interface AdvanceSpeakingSceneUseCase {
    fun execute(command: AdvanceSpeakingSceneCommand): SpeakingSessionViewResult
}
