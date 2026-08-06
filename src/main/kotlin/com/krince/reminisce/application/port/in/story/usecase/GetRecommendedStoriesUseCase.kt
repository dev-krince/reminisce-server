package com.krince.reminisce.application.port.`in`.story.usecase

import com.krince.reminisce.application.port.`in`.story.command.GetRecommendedStoriesCommand
import com.krince.reminisce.application.port.`in`.story.result.StorySummaryResult

interface GetRecommendedStoriesUseCase {
    fun execute(command: GetRecommendedStoriesCommand): List<StorySummaryResult>
}
