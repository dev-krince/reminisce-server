package com.krince.reminisce.application.port.`in`.savedstory.usecase

import com.krince.reminisce.application.port.`in`.savedstory.command.AddStoryBookmarkCommand
import com.krince.reminisce.application.port.`in`.savedstory.result.BookmarkedStoryResult

interface AddStoryBookmarkUseCase {
    fun execute(command: AddStoryBookmarkCommand): BookmarkedStoryResult
}
