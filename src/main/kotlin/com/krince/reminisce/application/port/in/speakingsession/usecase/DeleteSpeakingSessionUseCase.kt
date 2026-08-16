package com.krince.reminisce.application.port.`in`.speakingsession.usecase

import com.krince.reminisce.application.port.`in`.speakingsession.command.DeleteSpeakingSessionCommand

interface DeleteSpeakingSessionUseCase {
    fun execute(command: DeleteSpeakingSessionCommand)
}
