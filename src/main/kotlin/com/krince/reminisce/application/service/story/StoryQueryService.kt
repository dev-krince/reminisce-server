package com.krince.reminisce.application.service.story

import com.krince.reminisce.application.port.access.child.ChildAccessPort
import com.krince.reminisce.application.port.access.savedstory.SavedStoryAccessPort
import com.krince.reminisce.application.port.`in`.story.command.GetStoriesCommand
import com.krince.reminisce.application.port.`in`.story.command.GetStoryCommand
import com.krince.reminisce.application.port.`in`.story.result.StoryDetailResult
import com.krince.reminisce.application.port.`in`.story.result.StorySummaryResult
import com.krince.reminisce.application.port.`in`.story.usecase.GetStoriesUseCase
import com.krince.reminisce.application.port.`in`.story.usecase.GetStoryUseCase
import com.krince.reminisce.application.port.out.story.LoadStoryPort
import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.story.Story
import com.krince.reminisce.domain.model.story.vo.StoryId
import com.krince.reminisce.domain.model.user.vo.UserId
import com.krince.reminisce.shared.exception.NotFoundException
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND_STORY
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class StoryQueryService(
    private val loadStoryPort: LoadStoryPort,
    private val savedStoryAccessPort: SavedStoryAccessPort,
    private val childAccessPort: ChildAccessPort,
) : GetStoriesUseCase, GetStoryUseCase {

    @Transactional(readOnly = true)
    override fun execute(command: GetStoriesCommand): List<StorySummaryResult> {
        val stories: List<Story> =
            loadStoryPort.findPublished(command.genre, command.topic, command.titleKeyword, command.sort)
        val bookmarkedStoryIds: Set<String> = resolveBookmarkedStoryIds(command)

        return stories.map { StorySummaryResult.from(it, it.storyId.value in bookmarkedStoryIds) }
    }

    private fun resolveBookmarkedStoryIds(command: GetStoriesCommand): Set<String> {
        val childId: String = command.childId ?: return emptySet()
        val ownedChildId = ChildId(childId)
        verifyOwnership(ownedChildId, UserId(command.guardianId ?: return emptySet()))

        return savedStoryAccessPort.findBookmarkedStoryIds(ownedChildId)
    }

    private fun verifyOwnership(childId: ChildId, guardianId: UserId) {
        val ownerId: UserId? = childAccessPort.findGuardianId(childId)
        if (ownerId == null || ownerId != guardianId) {
            throw NotFoundException(NOT_FOUND, NOT_FOUND.message)
        }
    }

    @Transactional(readOnly = true)
    override fun execute(command: GetStoryCommand): StoryDetailResult {
        val story: Story = loadStoryPort.findByIdWithScenesPublished(StoryId(command.storyId))
            ?: throw NotFoundException(NOT_FOUND_STORY, NOT_FOUND_STORY.message)

        return StoryDetailResult.from(story)
    }
}
