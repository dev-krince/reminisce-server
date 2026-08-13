package com.krince.reminisce.application.port.`in`.speakingsession.usecase

import com.krince.reminisce.application.port.`in`.speakingsession.command.GetSpeakingHintCommand
import com.krince.reminisce.application.port.`in`.speakingsession.result.SpeakingHintResult

interface GetSpeakingHintUseCase {
    fun execute(command: GetSpeakingHintCommand): SpeakingHintResult
}
