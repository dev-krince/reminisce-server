package com.krince.reminisce.application.port.`in`.auth.usecase

import com.krince.reminisce.application.port.`in`.auth.command.NaverLoginCommand
import com.krince.reminisce.application.port.`in`.auth.result.TokenResult

interface NaverLoginUseCase {
    fun execute(command: NaverLoginCommand): TokenResult
}
