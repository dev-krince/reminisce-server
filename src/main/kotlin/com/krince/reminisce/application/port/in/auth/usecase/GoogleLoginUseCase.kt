package com.krince.reminisce.application.port.`in`.auth.usecase

import com.krince.reminisce.application.port.`in`.auth.command.GoogleLoginCommand
import com.krince.reminisce.application.port.`in`.auth.result.TokenResult

interface GoogleLoginUseCase {
    fun execute(command: GoogleLoginCommand): TokenResult
}
