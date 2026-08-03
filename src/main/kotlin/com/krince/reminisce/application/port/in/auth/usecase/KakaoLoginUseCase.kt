package com.krince.reminisce.application.port.`in`.auth.usecase

import com.krince.reminisce.application.port.`in`.auth.command.KakaoLoginCommand
import com.krince.reminisce.application.port.`in`.auth.result.TokenResult

interface KakaoLoginUseCase {
    fun execute(command: KakaoLoginCommand): TokenResult
}
