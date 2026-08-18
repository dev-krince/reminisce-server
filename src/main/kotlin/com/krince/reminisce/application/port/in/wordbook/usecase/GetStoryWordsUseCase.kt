package com.krince.reminisce.application.port.`in`.wordbook.usecase

import com.krince.reminisce.application.port.`in`.wordbook.command.GetStoryWordsCommand
import com.krince.reminisce.application.port.`in`.wordbook.result.StoryWordGroupResult

interface GetStoryWordsUseCase {
    fun execute(command: GetStoryWordsCommand): List<StoryWordGroupResult>
}
