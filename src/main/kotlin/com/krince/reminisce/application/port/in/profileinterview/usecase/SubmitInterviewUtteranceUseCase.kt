package com.krince.reminisce.application.port.`in`.profileinterview.usecase

import com.krince.reminisce.application.port.`in`.profileinterview.command.SubmitInterviewUtteranceCommand
import com.krince.reminisce.application.port.`in`.profileinterview.result.ProfileInterviewResult

interface SubmitInterviewUtteranceUseCase {
    fun execute(command: SubmitInterviewUtteranceCommand): ProfileInterviewResult
}
