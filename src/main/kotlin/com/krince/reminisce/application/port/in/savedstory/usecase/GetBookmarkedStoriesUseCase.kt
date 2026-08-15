package com.krince.reminisce.application.port.`in`.savedstory.usecase

import com.krince.reminisce.application.port.`in`.savedstory.command.GetBookmarkedStoriesCommand
import com.krince.reminisce.application.port.`in`.savedstory.result.BookmarkedStoryResult

interface GetBookmarkedStoriesUseCase {
    fun execute(command: GetBookmarkedStoriesCommand): List<BookmarkedStoryResult>
}
