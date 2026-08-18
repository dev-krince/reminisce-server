package com.krince.reminisce.application.port.`in`.admin.usecase

import com.krince.reminisce.application.port.`in`.admin.command.UpdateInterviewStageTurnsCommand
import com.krince.reminisce.application.port.`in`.admin.result.InterviewStageTurnsResult

interface UpdateInterviewStageTurnsUseCase {
    fun execute(command: UpdateInterviewStageTurnsCommand): InterviewStageTurnsResult
}
