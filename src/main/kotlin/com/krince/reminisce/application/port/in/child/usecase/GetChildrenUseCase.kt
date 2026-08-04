package com.krince.reminisce.application.port.`in`.child.usecase

import com.krince.reminisce.application.port.`in`.child.command.GetChildrenCommand
import com.krince.reminisce.application.port.`in`.child.result.ChildResult

interface GetChildrenUseCase {
    fun execute(command: GetChildrenCommand): List<ChildResult>
}
