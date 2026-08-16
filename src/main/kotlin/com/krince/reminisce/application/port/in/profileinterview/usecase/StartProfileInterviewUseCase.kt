package com.krince.reminisce.application.port.`in`.profileinterview.usecase

import com.krince.reminisce.application.port.`in`.profileinterview.command.StartProfileInterviewCommand
import com.krince.reminisce.application.port.`in`.profileinterview.result.ProfileInterviewResult

interface StartProfileInterviewUseCase {
    fun execute(command: StartProfileInterviewCommand): ProfileInterviewResult
}
