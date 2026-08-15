package com.krince.reminisce.application.port.`in`.savedstory.usecase

import com.krince.reminisce.application.port.`in`.savedstory.command.RemoveStoryBookmarkCommand

interface RemoveStoryBookmarkUseCase {
    fun execute(command: RemoveStoryBookmarkCommand)
}
