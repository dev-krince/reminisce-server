package com.krince.reminisce.application.port.`in`.child.usecase

import com.krince.reminisce.application.port.`in`.child.command.DeleteChildCommand

interface DeleteChildUseCase {
    fun execute(command: DeleteChildCommand)
}
