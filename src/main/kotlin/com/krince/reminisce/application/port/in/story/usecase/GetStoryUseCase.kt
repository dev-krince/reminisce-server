package com.krince.reminisce.application.port.`in`.story.usecase

import com.krince.reminisce.application.port.`in`.story.command.GetStoryCommand
import com.krince.reminisce.application.port.`in`.story.result.StoryDetailResult

interface GetStoryUseCase {
    fun execute(command: GetStoryCommand): StoryDetailResult
}
