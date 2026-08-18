package com.krince.reminisce.application.port.`in`.admin.usecase

import com.krince.reminisce.application.port.`in`.admin.command.UpdateSceneTurnsCommand
import com.krince.reminisce.application.port.`in`.admin.result.SceneTurnsResult

interface UpdateSceneTurnsUseCase {
    fun execute(command: UpdateSceneTurnsCommand): SceneTurnsResult
}
