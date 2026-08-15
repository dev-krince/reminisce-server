package com.krince.reminisce.application.service.speakingsession

import com.krince.reminisce.application.port.access.child.ChildAccessPort
import com.krince.reminisce.application.port.`in`.speakingsession.command.StopSpeakingSessionCommand
import com.krince.reminisce.application.port.`in`.speakingsession.result.SpeakingSessionResult
import com.krince.reminisce.application.port.`in`.speakingsession.usecase.StopSpeakingSessionUseCase
import com.krince.reminisce.application.port.out.speakingsession.CommandSpeakingSessionPort
import com.krince.reminisce.application.port.out.speakingsession.LoadSpeakingSessionPort
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
class StopSpeakingSessionApplicationService(
    private val loadSpeakingSessionPort: LoadSpeakingSessionPort,
    private val commandSpeakingSessionPort: CommandSpeakingSessionPort,
    private val childAccessPort: ChildAccessPort,
    private val clock: Clock,
) : StopSpeakingSessionUseCase {

    @Transactional
    override fun execute(command: StopSpeakingSessionCommand): SpeakingSessionResult {
        val session: SpeakingSession = loadOwnedSession(command.sessionId, command.guardianId)
        if (session.status !in STOPPABLE_STATUSES) {
            throw BusinessRuleViolationException(BUSINESS_RULE_VIOLATION, BUSINESS_RULE_VIOLATION.message)
        }
        val stopped: SpeakingSession = commandSpeakingSessionPort.save(session.stop(LocalDateTime.now(clock)))

        return SpeakingSessionResult.from(stopped, created = false)
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

    companion object {
        private val STOPPABLE_STATUSES: Set<SessionStatus> = setOf(
            SessionStatus.IN_PROGRESS,
            SessionStatus.POST_ACTIVITY,
        )
    }
}
