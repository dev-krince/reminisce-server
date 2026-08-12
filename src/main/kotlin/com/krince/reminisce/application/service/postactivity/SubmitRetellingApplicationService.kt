package com.krince.reminisce.application.service.postactivity

import com.krince.reminisce.application.port.access.child.ChildAccessPort
import com.krince.reminisce.application.port.`in`.postactivity.command.SubmitRetellingCommand
import com.krince.reminisce.application.port.`in`.postactivity.result.RetellingResult
import com.krince.reminisce.application.port.`in`.postactivity.usecase.SubmitRetellingUseCase
import com.krince.reminisce.application.port.out.postactivityresult.CommandPostActivityResultPort
import com.krince.reminisce.application.port.out.postactivityresult.LoadPostActivityResultPort
import com.krince.reminisce.application.port.out.speakingsession.CommandSpeakingSessionPort
import com.krince.reminisce.application.port.out.speakingsession.LoadSpeakingSessionPort
import com.krince.reminisce.domain.model.postactivityresult.PostActivityResult
import com.krince.reminisce.domain.model.speakingsession.SpeakingSession
import com.krince.reminisce.domain.model.speakingsession.vo.SessionStatus
import com.krince.reminisce.domain.model.speakingsession.vo.SpeakingSessionId
import com.krince.reminisce.domain.model.user.vo.UserId
import com.krince.reminisce.shared.exception.BusinessRuleViolationException
import com.krince.reminisce.shared.exception.NotFoundException
import com.krince.reminisce.shared.response.ExceptionResponseCode.BUSINESS_RULE_VIOLATION
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDateTime

@Service
class SubmitRetellingApplicationService(
    private val loadSpeakingSessionPort: LoadSpeakingSessionPort,
    private val childAccessPort: ChildAccessPort,
    private val loadPostActivityResultPort: LoadPostActivityResultPort,
    private val commandPostActivityResultPort: CommandPostActivityResultPort,
    private val commandSpeakingSessionPort: CommandSpeakingSessionPort,
    private val clock: Clock,
) : SubmitRetellingUseCase {

    @Transactional
    override fun execute(command: SubmitRetellingCommand): RetellingResult {
        val session: SpeakingSession = loadOwnedSession(command.sessionId, command.guardianId)
        if (session.status != SessionStatus.POST_ACTIVITY) {
            throw BusinessRuleViolationException(BUSINESS_RULE_VIOLATION, BUSINESS_RULE_VIOLATION.message)
        }

        val existing: PostActivityResult = loadPostActivityResultPort.findBySession(session.sessionId)
            ?: throw BusinessRuleViolationException(BUSINESS_RULE_VIOLATION, BUSINESS_RULE_VIOLATION.message)
        if (existing.isOrderCorrect != true) {
            throw BusinessRuleViolationException(BUSINESS_RULE_VIOLATION, BUSINESS_RULE_VIOLATION.message)
        }

        val transcript: String = command.text.trim()

        val now: LocalDateTime = LocalDateTime.now(clock)
        commandPostActivityResultPort.save(existing.completeWith(transcript, command.retellingAudioUrl, now))
        commandSpeakingSessionPort.save(session.complete(now))

        return RetellingResult(
            retellingText = transcript,
            retellingAudioUrl = command.retellingAudioUrl,
            completedAt = now,
            status = SessionStatus.COMPLETED,
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
