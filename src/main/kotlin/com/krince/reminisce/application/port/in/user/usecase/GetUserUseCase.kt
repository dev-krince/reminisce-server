package com.krince.reminisce.application.port.`in`.user.usecase

import com.krince.reminisce.application.port.access.user.context.UserResult
import com.krince.reminisce.application.port.`in`.user.command.GetUserCommand

interface GetUserUseCase {
    fun execute(command: GetUserCommand): UserResult
}