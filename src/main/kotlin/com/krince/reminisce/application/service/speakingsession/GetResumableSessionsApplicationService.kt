package com.krince.reminisce.application.service.speakingsession

import com.krince.reminisce.application.port.access.child.ChildAccessPort
import com.krince.reminisce.application.port.access.story.ResumableStoryDisplayInfo
import com.krince.reminisce.application.port.access.story.StoryAccessPort
import com.krince.reminisce.application.port.`in`.speakingsession.command.GetResumableSessionsCommand
import com.krince.reminisce.application.port.`in`.speakingsession.result.SpeakingSessionSummaryResult
import com.krince.reminisce.application.port.`in`.speakingsession.usecase.GetResumableSessionsUseCase
import com.krince.reminisce.application.port.out.speakingsession.LoadSpeakingSessionPort
import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.speakingsession.SpeakingSession
import com.krince.reminisce.domain.model.user.vo.UserId
import com.krince.reminisce.shared.exception.NotFoundException
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetResumableSessionsApplicationService(
    private val loadSpeakingSessionPort: LoadSpeakingSessionPort,
    private val childAccessPort: ChildAccessPort,
    private val storyAccessPort: StoryAccessPort,
) : GetResumableSessionsUseCase {

    @Transactional(readOnly = true)
    override fun execute(command: GetResumableSessionsCommand): List<SpeakingSessionSummaryResult> {
        val childId = ChildId(command.childId)
        verifyOwnership(childId, UserId(command.guardianId))

        return loadSpeakingSessionPort.findResumableByChild(childId)
            .map { SpeakingSessionSummaryResult.from(it, displayInfoOf(it)) }
    }

    private fun displayInfoOf(session: SpeakingSession): ResumableStoryDisplayInfo =
        storyAccessPort.findResumableDisplayInfo(session.storyId, session.currentSceneId) ?: EMPTY_DISPLAY_INFO

    private fun verifyOwnership(childId: ChildId, guardianId: UserId) {
        val ownerId: UserId? = childAccessPort.findGuardianId(childId)
        if (ownerId == null || ownerId != guardianId) {
            throw NotFoundException(NOT_FOUND, NOT_FOUND.message)
        }
    }

    companion object {
        private const val NO_CHAPTER: Int = 0
        private val EMPTY_DISPLAY_INFO = ResumableStoryDisplayInfo(
            title = "",
            representativeImageUrl = null,
            difficulty = "",
            topics = emptyList(),
            currentChapter = NO_CHAPTER,
            totalChapters = NO_CHAPTER,
        )
    }
}
