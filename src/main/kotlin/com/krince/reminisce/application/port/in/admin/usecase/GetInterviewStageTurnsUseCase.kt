package com.krince.reminisce.application.port.`in`.admin.usecase

import com.krince.reminisce.application.port.`in`.admin.result.InterviewStageTurnsResult

interface GetInterviewStageTurnsUseCase {
    fun execute(): InterviewStageTurnsResult
}
