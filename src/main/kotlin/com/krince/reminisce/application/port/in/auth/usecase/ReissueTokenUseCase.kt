package com.krince.reminisce.application.port.`in`.auth.usecase

import com.krince.reminisce.application.port.`in`.auth.command.ReissueTokenCommand
import com.krince.reminisce.application.port.`in`.auth.result.TokenResult

interface ReissueTokenUseCase {
    fun execute(command: ReissueTokenCommand): TokenResult
}
