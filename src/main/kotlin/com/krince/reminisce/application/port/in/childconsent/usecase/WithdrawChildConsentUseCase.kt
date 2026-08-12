package com.krince.reminisce.application.port.`in`.childconsent.usecase

import com.krince.reminisce.application.port.`in`.childconsent.command.WithdrawChildConsentCommand

interface WithdrawChildConsentUseCase {
    fun execute(command: WithdrawChildConsentCommand)
}
