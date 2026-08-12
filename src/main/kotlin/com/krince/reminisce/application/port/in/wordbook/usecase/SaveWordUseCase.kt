package com.krince.reminisce.application.port.`in`.wordbook.usecase

import com.krince.reminisce.application.port.`in`.wordbook.command.SaveWordCommand
import com.krince.reminisce.application.port.`in`.wordbook.result.SavedWordResult

interface SaveWordUseCase {
    fun execute(command: SaveWordCommand): SavedWordResult
}
