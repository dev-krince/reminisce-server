package com.krince.boilerplate.application.port.`in`.user.usecase

import com.krince.boilerplate.application.port.access.user.context.UserResult
import com.krince.boilerplate.application.port.`in`.user.command.GetUserCommand

interface GetUserUseCase {
    fun execute(command: GetUserCommand): UserResult
}