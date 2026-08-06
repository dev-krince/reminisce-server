package com.krince.reminisce.application.service.speakingsession

import com.krince.reminisce.application.port.access.child.ChildAccessPort
import com.krince.reminisce.application.port.access.story.StoryAccessPort
import com.krince.reminisce.application.port.`in`.speakingsession.command.AdvanceSpeakingSceneCommand
import com.krince.reminisce.application.port.`in`.speakingsession.result.SpeakingSessionViewResult
import com.krince.reminisce.application.port.`in`.speakingsession.usecase.AdvanceSpeakingSceneUseCase
import com.krince.reminisce.application.port.out.speakingsession.CommandSpeakingSessionPort
import com.krince.reminisce.application.port.out.speakingsession.LoadSpeakingSessionPort
import com.krince.reminisce.domain.model.speakingsession.SpeakingSession
import com.krince.reminisce.domain.model.speakingsession.vo.SpeakingSessionId
import com.krince.reminisce.domain.model.story.Scene
import com.krince.reminisce.domain.model.story.vo.StoryId
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
class AdvanceSpeakingSceneApplicationService(
    private val loadSpeakingSessionPort: LoadSpeakingSessionPort,
    private val commandSpeakingSessionPort: CommandSpeakingSessionPort,
    private val childAccessPort: ChildAccessPort,
    private val storyAccessPort: StoryAccessPort,
    private val clock: Clock,
) : AdvanceSpeakingSceneUseCase {

    @Transactional
    override fun execute(command: AdvanceSpeakingSceneCommand): SpeakingSessionViewResult {
        val session: SpeakingSession = loadOwnedSession(command.sessionId, command.guardianId)
        verifyAtIntro(session)

        val storyId: StoryId = session.storyId
        val firstSceneId: String = storyAccessPort.findFirstSceneId(storyId)
            ?: throw NotFoundException(NOT_FOUND, NOT_FOUND.message)
        val scene: Scene = storyAccessPort.findScene(storyId, firstSceneId)
            ?: throw NotFoundException(NOT_FOUND, NOT_FOUND.message)

        val advanced: SpeakingSession = session.advanceToScene(firstSceneId, LocalDateTime.now(clock))
        commandSpeakingSessionPort.save(advanced)

        return SpeakingSessionViewResult.scene(scene)
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

    private fun verifyAtIntro(session: SpeakingSession) {
        if (session.currentSceneId != null) {
            throw BusinessRuleViolationException(BUSINESS_RULE_VIOLATION, BUSINESS_RULE_VIOLATION.message)
        }
    }
}
