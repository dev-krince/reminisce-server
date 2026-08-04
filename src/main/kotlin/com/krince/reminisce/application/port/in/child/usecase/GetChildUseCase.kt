package com.krince.reminisce.application.port.`in`.child.usecase

import com.krince.reminisce.application.port.`in`.child.command.GetChildCommand
import com.krince.reminisce.application.port.`in`.child.result.ChildResult

interface GetChildUseCase {
    fun execute(command: GetChildCommand): ChildResult
}
