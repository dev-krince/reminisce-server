package com.krince.reminisce.application.port.`in`.user.usecase

import com.krince.reminisce.application.port.`in`.user.command.WithdrawGuardianCommand

interface WithdrawGuardianUseCase {
    fun execute(command: WithdrawGuardianCommand)
}
