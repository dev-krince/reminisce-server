package com.krince.reminisce.application.port.`in`.child.usecase

import com.krince.reminisce.application.port.`in`.child.command.RegisterChildCommand
import com.krince.reminisce.application.port.`in`.child.result.ChildResult

interface RegisterChildUseCase {
    fun execute(command: RegisterChildCommand): ChildResult
}
