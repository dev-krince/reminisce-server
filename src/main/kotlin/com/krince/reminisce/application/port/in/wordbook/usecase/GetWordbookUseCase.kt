package com.krince.reminisce.application.port.`in`.wordbook.usecase

import com.krince.reminisce.application.port.`in`.wordbook.command.GetWordbookCommand
import com.krince.reminisce.application.port.`in`.wordbook.result.SavedWordResult

interface GetWordbookUseCase {
    fun execute(command: GetWordbookCommand): List<SavedWordResult>
}
