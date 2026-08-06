package com.krince.reminisce.application.service.postactivity

import com.krince.reminisce.application.port.access.child.ChildAccessPort
import com.krince.reminisce.application.port.access.story.StoryAccessPort
import com.krince.reminisce.application.port.`in`.postactivity.command.SubmitCardOrderCommand
import com.krince.reminisce.application.port.`in`.postactivity.result.CardOrderResult
import com.krince.reminisce.application.port.`in`.postactivity.usecase.SubmitCardOrderUseCase
import com.krince.reminisce.application.port.out.postactivityresult.CommandPostActivityResultPort
import com.krince.reminisce.application.port.out.postactivityresult.LoadPostActivityResultPort
import com.krince.reminisce.application.port.out.speakingsession.LoadSpeakingSessionPort
import com.krince.reminisce.domain.model.postactivityresult.PostActivityResult
import com.krince.reminisce.domain.model.speakingsession.SpeakingSession
import com.krince.reminisce.domain.model.speakingsession.vo.SessionStatus
import com.krince.reminisce.domain.model.speakingsession.vo.SpeakingSessionId
import com.krince.reminisce.domain.model.story.vo.PostActivityConfig
import com.krince.reminisce.domain.model.user.vo.UserId
import com.krince.reminisce.shared.exception.BusinessRuleViolationException
import com.krince.reminisce.shared.exception.NotFoundException
import com.krince.reminisce.shared.response.ExceptionResponseCode.BUSINESS_RULE_VIOLATION
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SubmitCardOrderApplicationService(
    private val loadSpeakingSessionPort: LoadSpeakingSessionPort,
    private val childAccessPort: ChildAccessPort,
    private val storyAccessPort: StoryAccessPort,
    private val loadPostActivityResultPort: LoadPostActivityResultPort,
    private val commandPostActivityResultPort: CommandPostActivityResultPort,
) : SubmitCardOrderUseCase {

    @Transactional
    override fun execute(command: SubmitCardOrderCommand): CardOrderResult {
        val session: SpeakingSession = loadOwnedSession(command.sessionId, command.guardianId)
        if (session.status != SessionStatus.POST_ACTIVITY) {
            throw BusinessRuleViolationException(BUSINESS_RULE_VIOLATION, BUSINESS_RULE_VIOLATION.message)
        }

        val config: PostActivityConfig = storyAccessPort.findPostActivityConfig(session.storyId)
            ?: throw NotFoundException(NOT_FOUND, NOT_FOUND.message)

        val correctSequence: List<String> = config.cards.sortedBy { it.correctOrder }.map { it.id }
        val isOrderCorrect: Boolean = command.order == correctSequence

        val existing: PostActivityResult? = loadPostActivityResultPort.findBySession(session.sessionId)
        val toSave: PostActivityResult = existing?.resubmit(command.order, isOrderCorrect)
            ?: PostActivityResult.firstSubmission(session.sessionId, command.order, isOrderCorrect)
        val saved: PostActivityResult = commandPostActivityResultPort.save(toSave)

        val keywords: List<String> = if (isOrderCorrect) config.retellingKeywords else emptyList()

        return CardOrderResult(
            isOrderCorrect = isOrderCorrect,
            attemptCount = saved.attemptCount,
            retellingKeywords = keywords,
        )
    }

    private fun loadOwnedSession(sessionId: String, guardianId: String): SpeakingSession {
        val session: SpeakingSession = loadSpeakingSessionPort.findById(SpeakingSessionId(sessionId))
            ?: throw NotFoundException(NOT_FOUND, NOT_FOUND.message)
        verifyOwnership(session, UserId(guardianId))

        return session
    }

    private fun verifyOwnership(session: SpeakingSession, guardianId: UserId) {
        val ownerId: UserId? = childAccessPort.findGuardianId(session.childId)
        if (ownerId == null || ownerId != guardianId) {
            throw NotFoundException(NOT_FOUND, NOT_FOUND.message)
        }
    }
}
