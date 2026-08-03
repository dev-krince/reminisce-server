package com.krince.reminisce.application.port.`in`.auth.usecase

import com.krince.reminisce.application.port.`in`.auth.command.LoginCommand
import com.krince.reminisce.application.port.`in`.auth.result.TokenResult

interface LoginUseCase {
    fun execute(command: LoginCommand): TokenResult
}
