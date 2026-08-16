package com.krince.reminisce.application.port.`in`.mission.usecase

import com.krince.reminisce.application.port.`in`.mission.command.SubmitMissionAnswerCommand
import com.krince.reminisce.application.port.`in`.mission.result.MissionAnswerResult

interface SubmitMissionAnswerUseCase {
    fun execute(command: SubmitMissionAnswerCommand): MissionAnswerResult
}
