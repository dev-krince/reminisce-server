package com.krince.reminisce.application.port.`in`.postactivity.usecase

import com.krince.reminisce.application.port.`in`.postactivity.command.SubmitCardOrderCommand
import com.krince.reminisce.application.port.`in`.postactivity.result.CardOrderResult

interface SubmitCardOrderUseCase {
    fun execute(command: SubmitCardOrderCommand): CardOrderResult
}
