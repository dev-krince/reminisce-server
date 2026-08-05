package com.krince.reminisce.application.port.`in`.story.usecase

import com.krince.reminisce.application.port.`in`.story.command.GetStoriesCommand
import com.krince.reminisce.application.port.`in`.story.result.StorySummaryResult

interface GetStoriesUseCase {
    fun execute(command: GetStoriesCommand): List<StorySummaryResult>
}
