package com.krince.reminisce.application.service.savedstory

import com.krince.reminisce.application.port.access.child.ChildAccessPort
import com.krince.reminisce.application.port.access.story.StoryAccessPort
import com.krince.reminisce.application.port.`in`.savedstory.command.AddStoryBookmarkCommand
import com.krince.reminisce.application.port.`in`.savedstory.command.GetBookmarkedStoriesCommand
import com.krince.reminisce.application.port.`in`.savedstory.command.RemoveStoryBookmarkCommand
import com.krince.reminisce.application.port.`in`.savedstory.result.BookmarkedStoryResult
import com.krince.reminisce.application.port.`in`.savedstory.usecase.AddStoryBookmarkUseCase
import com.krince.reminisce.application.port.`in`.savedstory.usecase.GetBookmarkedStoriesUseCase
import com.krince.reminisce.application.port.`in`.savedstory.usecase.RemoveStoryBookmarkUseCase
import com.krince.reminisce.application.port.out.savedstory.CommandSavedStoryPort
import com.krince.reminisce.application.port.out.savedstory.LoadSavedStoryPort
import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.savedstory.SavedStory
import com.krince.reminisce.domain.model.story.vo.StoryId
import com.krince.reminisce.domain.model.user.vo.UserId
import com.krince.reminisce.shared.exception.NotFoundException
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND_STORY
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class StoryBookmarkApplicationService(
    private val loadSavedStoryPort: LoadSavedStoryPort,
    private val commandSavedStoryPort: CommandSavedStoryPort,
    private val childAccessPort: ChildAccessPort,
    private val storyAccessPort: StoryAccessPort,
) : AddStoryBookmarkUseCase, RemoveStoryBookmarkUseCase, GetBookmarkedStoriesUseCase {

    @Transactional
    override fun execute(command: AddStoryBookmarkCommand): BookmarkedStoryResult {
        val childId = ChildId(command.childId)
        verifyOwnership(childId, UserId(command.guardianId))

        val storyId = StoryId(command.storyId)
        verifyPublishedStory(storyId)

        val bookmark: SavedStory = loadSavedStoryPort.findByChildIdAndStoryId(childId, storyId)
            ?: commandSavedStoryPort.saveIfAbsent(SavedStory.create(childId = childId, storyId = storyId))

        return BookmarkedStoryResult.from(bookmark)
    }

    @Transactional
    override fun execute(command: RemoveStoryBookmarkCommand) {
        val childId = ChildId(command.childId)
        verifyOwnership(childId, UserId(command.guardianId))

        commandSavedStoryPort.deleteByChildIdAndStoryId(childId, StoryId(command.storyId))
    }

    @Transactional(readOnly = true)
    override fun execute(command: GetBookmarkedStoriesCommand): List<BookmarkedStoryResult> {
        val childId = ChildId(command.childId)
        verifyOwnership(childId, UserId(command.guardianId))

        return loadSavedStoryPort.findAllByChildId(childId).map { BookmarkedStoryResult.from(it) }
    }

    private fun verifyOwnership(childId: ChildId, guardianId: UserId) {
        val ownerId: UserId? = childAccessPort.findGuardianId(childId)
        if (ownerId == null || ownerId != guardianId) {
            throw NotFoundException(NOT_FOUND, NOT_FOUND.message)
        }
    }

    private fun verifyPublishedStory(storyId: StoryId) {
        if (!storyAccessPort.existsPublished(storyId)) {
            throw NotFoundException(NOT_FOUND_STORY, NOT_FOUND_STORY.message)
        }
    }
}
