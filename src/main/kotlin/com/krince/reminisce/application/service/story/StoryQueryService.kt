package com.krince.reminisce.application.service.story

import com.krince.reminisce.application.port.`in`.story.command.GetStoriesCommand
import com.krince.reminisce.application.port.`in`.story.command.GetStoryCommand
import com.krince.reminisce.application.port.`in`.story.result.StoryDetailResult
import com.krince.reminisce.application.port.`in`.story.result.StorySummaryResult
import com.krince.reminisce.application.port.`in`.story.usecase.GetStoriesUseCase
import com.krince.reminisce.application.port.`in`.story.usecase.GetStoryUseCase
import com.krince.reminisce.application.port.out.story.LoadStoryPort
import com.krince.reminisce.domain.model.story.Story
import com.krince.reminisce.domain.model.story.vo.StoryId
import com.krince.reminisce.shared.exception.NotFoundException
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND_STORY
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class StoryQueryService(
    private val loadStoryPort: LoadStoryPort,
) : GetStoriesUseCase, GetStoryUseCase {

    @Transactional(readOnly = true)
    override fun execute(command: GetStoriesCommand): List<StorySummaryResult> {
        val topic: String? = command.topic
        if (topic == null) {
            return loadStoryPort.findAllPublished().map { StorySummaryResult.from(it) }
        }

        return loadStoryPort.findAllPublishedByTopic(topic).map { StorySummaryResult.from(it) }
    }

    @Transactional(readOnly = true)
    override fun execute(command: GetStoryCommand): StoryDetailResult {
        val story: Story = loadStoryPort.findByIdWithScenesPublished(StoryId(command.storyId))
            ?: throw NotFoundException(NOT_FOUND_STORY, NOT_FOUND_STORY.message)

        return StoryDetailResult.from(story)
    }
}
