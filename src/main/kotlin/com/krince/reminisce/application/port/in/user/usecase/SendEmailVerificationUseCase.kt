package com.krince.reminisce.application.port.`in`.user.usecase

import com.krince.reminisce.application.port.`in`.user.command.SendEmailVerificationCommand

interface SendEmailVerificationUseCase {
    fun execute(command: SendEmailVerificationCommand)
}
