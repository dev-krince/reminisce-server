package com.krince.reminisce.application.port.`in`.message.usecase

import com.krince.reminisce.application.port.`in`.message.command.SubmitUtteranceCommand
import com.krince.reminisce.application.port.`in`.message.result.UtteranceResult

interface SubmitUtteranceUseCase {
    fun execute(command: SubmitUtteranceCommand): UtteranceResult
}
