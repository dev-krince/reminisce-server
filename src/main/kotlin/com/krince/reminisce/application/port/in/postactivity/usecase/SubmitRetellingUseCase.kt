package com.krince.reminisce.application.port.`in`.postactivity.usecase

import com.krince.reminisce.application.port.`in`.postactivity.command.SubmitRetellingCommand
import com.krince.reminisce.application.port.`in`.postactivity.result.RetellingResult

interface SubmitRetellingUseCase {
    fun execute(command: SubmitRetellingCommand): RetellingResult
}
