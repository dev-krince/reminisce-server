package com.krince.reminisce.application.port.`in`.auth.usecase

import com.krince.reminisce.application.port.`in`.auth.command.LogoutCommand

interface LogoutUseCase {
    fun execute(command: LogoutCommand)
}
