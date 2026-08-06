package com.krince.reminisce.application.service.story

import com.krince.reminisce.application.port.access.child.ChildAccessPort
import com.krince.reminisce.application.port.access.speakingsession.SpeakingSessionAccessPort
import com.krince.reminisce.application.port.`in`.story.command.GetRecommendedStoriesCommand
import com.krince.reminisce.application.port.`in`.story.result.StorySummaryResult
import com.krince.reminisce.application.port.`in`.story.usecase.GetRecommendedStoriesUseCase
import com.krince.reminisce.application.port.out.story.LoadStoryPort
import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.story.Story
import com.krince.reminisce.domain.model.user.vo.UserId
import com.krince.reminisce.shared.exception.NotFoundException
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RecommendStoriesApplicationService(
    private val loadStoryPort: LoadStoryPort,
    private val speakingSessionAccessPort: SpeakingSessionAccessPort,
    private val childAccessPort: ChildAccessPort,
) : GetRecommendedStoriesUseCase {

    private companion object {
        const val MAX_RECOMMENDATIONS = 10
    }

    @Transactional(readOnly = true)
    override fun execute(command: GetRecommendedStoriesCommand): List<StorySummaryResult> {
        val childId = ChildId(command.childId)
        verifyOwnership(childId, UserId(command.guardianId))

        val startedStoryIds: Set<String> = speakingSessionAccessPort.findStartedStoryIds(childId).toSet()
        val publishedStories: List<Story> = loadStoryPort.findAllPublished()

        return publishedStories
            .filter { it.storyId.value !in startedStoryIds }
            .sortedBy { it.difficulty.value }
            .take(MAX_RECOMMENDATIONS)
            .map { StorySummaryResult.from(it) }
    }

    private fun verifyOwnership(childId: ChildId, guardianId: UserId) {
        val ownerId: UserId? = childAccessPort.findGuardianId(childId)
        if (ownerId == null || ownerId != guardianId) {
            throw NotFoundException(NOT_FOUND, NOT_FOUND.message)
        }
    }
}
