package com.krince.reminisce.application.port.`in`.user.usecase

import com.krince.reminisce.application.port.`in`.user.command.ConfirmEmailVerificationCommand

interface ConfirmEmailVerificationUseCase {
    fun execute(command: ConfirmEmailVerificationCommand)
}
