package com.krince.reminisce.application.service.speakingsession

import com.krince.reminisce.application.port.access.child.ChildAccessPort
import com.krince.reminisce.application.port.access.childconsent.ChildConsentAccessPort
import com.krince.reminisce.application.port.access.story.StoryAccessPort
import com.krince.reminisce.application.port.`in`.speakingsession.command.StartSpeakingSessionCommand
import com.krince.reminisce.application.port.`in`.speakingsession.result.SpeakingSessionResult
import com.krince.reminisce.application.port.`in`.speakingsession.usecase.StartSpeakingSessionUseCase
import com.krince.reminisce.application.port.out.speakingsession.CommandSpeakingSessionPort
import com.krince.reminisce.application.port.out.speakingsession.LoadSpeakingSessionPort
import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.speakingsession.SpeakingSession
import com.krince.reminisce.domain.model.story.vo.StoryId
import com.krince.reminisce.domain.model.user.vo.UserId
import com.krince.reminisce.shared.exception.BusinessRuleViolationException
import com.krince.reminisce.shared.exception.NotFoundException
import com.krince.reminisce.shared.response.ExceptionResponseCode.CONSENT_REQUIRED
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND_CHILD
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND_STORY
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDateTime

@Service
class StartSpeakingSessionApplicationService(
    private val childAccessPort: ChildAccessPort,
    private val childConsentAccessPort: ChildConsentAccessPort,
    private val storyAccessPort: StoryAccessPort,
    private val loadSpeakingSessionPort: LoadSpeakingSessionPort,
    private val commandSpeakingSessionPort: CommandSpeakingSessionPort,
    private val clock: Clock,
) : StartSpeakingSessionUseCase {

    @Transactional
    override fun execute(command: StartSpeakingSessionCommand): SpeakingSessionResult {
        val childId = ChildId(command.childId)
        val storyId = StoryId(command.storyId)

        verifyOwnership(childId, UserId(command.guardianId))
        verifyActiveConsent(childId)
        verifyPublishedStory(storyId)

        val existing: SpeakingSession? = loadSpeakingSessionPort.findInProgress(childId, storyId)
        if (existing != null) {
            return SpeakingSessionResult.from(existing, created = false)
        }

        val started: SpeakingSession = SpeakingSession.start(childId, storyId, LocalDateTime.now(clock))
        val saved: SpeakingSession = commandSpeakingSessionPort.save(started)

        return SpeakingSessionResult.from(saved, created = true)
    }

    private fun verifyOwnership(childId: ChildId, guardianId: UserId) {
        val ownerId: UserId? = childAccessPort.findGuardianId(childId)
        if (ownerId == null || ownerId != guardianId) {
            throw NotFoundException(NOT_FOUND_CHILD, NOT_FOUND_CHILD.message)
        }
    }

    private fun verifyActiveConsent(childId: ChildId) {
        if (!childConsentAccessPort.hasActiveConsent(childId)) {
            throw BusinessRuleViolationException(CONSENT_REQUIRED, CONSENT_REQUIRED.message)
        }
    }

    private fun verifyPublishedStory(storyId: StoryId) {
        if (!storyAccessPort.existsPublished(storyId)) {
            throw NotFoundException(NOT_FOUND_STORY, NOT_FOUND_STORY.message)
        }
    }
}
