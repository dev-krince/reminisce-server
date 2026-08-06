package com.krince.reminisce.application.service.speakingsession

import com.krince.reminisce.application.port.access.child.ChildAccessPort
import com.krince.reminisce.application.port.access.story.StoryAccessPort
import com.krince.reminisce.application.port.`in`.speakingsession.command.AdvanceSpeakingSceneCommand
import com.krince.reminisce.application.port.`in`.speakingsession.result.SpeakingSessionViewResult
import com.krince.reminisce.application.port.`in`.speakingsession.usecase.AdvanceSpeakingSceneUseCase
import com.krince.reminisce.application.port.out.speakingsession.CommandSpeakingSessionPort
import com.krince.reminisce.application.port.out.speakingsession.LoadSpeakingSessionPort
import com.krince.reminisce.domain.model.speakingsession.SpeakingSession
import com.krince.reminisce.domain.model.speakingsession.vo.SessionStatus
import com.krince.reminisce.domain.model.speakingsession.vo.SpeakingSessionId
import com.krince.reminisce.domain.model.story.Scene
import com.krince.reminisce.domain.model.story.vo.SceneType
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
        if (session.status != SessionStatus.IN_PROGRESS) {
            throw BusinessRuleViolationException(BUSINESS_RULE_VIOLATION, BUSINESS_RULE_VIOLATION.message)
        }
        val currentSceneId: String = session.currentSceneId
            ?: return advanceToFirstScene(session)

        val currentScene: Scene = storyAccessPort.findScene(session.storyId, currentSceneId)
            ?: throw NotFoundException(NOT_FOUND, NOT_FOUND.message)
        verifyLeavable(session, currentScene)

        return advanceToNext(session, currentSceneId)
    }

    private fun advanceToFirstScene(session: SpeakingSession): SpeakingSessionViewResult {
        val storyId: StoryId = session.storyId
        val firstSceneId: String = storyAccessPort.findFirstSceneId(storyId)
            ?: throw NotFoundException(NOT_FOUND, NOT_FOUND.message)
        val firstScene: Scene = storyAccessPort.findScene(storyId, firstSceneId)
            ?: throw NotFoundException(NOT_FOUND, NOT_FOUND.message)

        return transitionTo(session, firstScene)
    }

    private fun advanceToNext(session: SpeakingSession, currentSceneId: String): SpeakingSessionViewResult {
        val nextScene: Scene? = storyAccessPort.findNextScene(session.storyId, currentSceneId)
        if (nextScene == null) {
            commandSpeakingSessionPort.save(session.enterPostActivity(LocalDateTime.now(clock)))

            return SpeakingSessionViewResult.postActivity()
        }

        return transitionTo(session, nextScene)
    }

    private fun transitionTo(session: SpeakingSession, scene: Scene): SpeakingSessionViewResult {
        val transitioned: SpeakingSession = session.transitionToScene(scene.sceneId.value, LocalDateTime.now(clock))
        commandSpeakingSessionPort.save(transitioned)

        return SpeakingSessionViewResult.scene(scene)
    }

    private fun verifyLeavable(session: SpeakingSession, currentScene: Scene) {
        if (currentScene.sceneType != SceneType.DIALOGUE) {
            return
        }
        if (session.sceneEndReason != null) {
            return
        }

        throw BusinessRuleViolationException(BUSINESS_RULE_VIOLATION, BUSINESS_RULE_VIOLATION.message)
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
