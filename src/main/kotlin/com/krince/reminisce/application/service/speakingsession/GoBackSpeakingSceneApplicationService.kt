package com.krince.reminisce.application.service.speakingsession

import com.krince.reminisce.application.port.access.child.ChildAccessPort
import com.krince.reminisce.application.port.access.story.StoryAccessPort
import com.krince.reminisce.application.port.`in`.speakingsession.command.GoBackSpeakingSceneCommand
import com.krince.reminisce.application.port.`in`.speakingsession.result.SpeakingSessionViewResult
import com.krince.reminisce.application.port.`in`.speakingsession.usecase.GoBackSpeakingSceneUseCase
import com.krince.reminisce.application.port.out.speakingsession.CommandSpeakingSessionPort
import com.krince.reminisce.application.port.out.speakingsession.LoadSpeakingSessionPort
import com.krince.reminisce.application.port.out.tts.NARRATOR_VOICE_PROFILE
import com.krince.reminisce.application.port.out.tts.TtsPort
import com.krince.reminisce.domain.model.speakingsession.SpeakingSession
import com.krince.reminisce.domain.model.speakingsession.vo.SessionStatus
import com.krince.reminisce.domain.model.speakingsession.vo.SpeakingSessionId
import com.krince.reminisce.domain.model.story.Scene
import com.krince.reminisce.domain.model.story.vo.SceneType
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
class GoBackSpeakingSceneApplicationService(
    private val loadSpeakingSessionPort: LoadSpeakingSessionPort,
    private val commandSpeakingSessionPort: CommandSpeakingSessionPort,
    private val childAccessPort: ChildAccessPort,
    private val storyAccessPort: StoryAccessPort,
    private val ttsPort: TtsPort,
    private val clock: Clock,
) : GoBackSpeakingSceneUseCase {

    @Transactional
    override fun execute(command: GoBackSpeakingSceneCommand): SpeakingSessionViewResult {
        val session: SpeakingSession = loadOwnedSession(command.sessionId, command.guardianId)
        if (session.status != SessionStatus.IN_PROGRESS) {
            throw BusinessRuleViolationException(BUSINESS_RULE_VIOLATION, BUSINESS_RULE_VIOLATION.message)
        }
        val currentSceneId: String = session.currentSceneId
            ?: throw BusinessRuleViolationException(BUSINESS_RULE_VIOLATION, BUSINESS_RULE_VIOLATION.message)
        val previousChapterFirstScene: Scene =
            storyAccessPort.findPreviousChapterFirstScene(session.storyId, currentSceneId)
                ?: throw BusinessRuleViolationException(BUSINESS_RULE_VIOLATION, BUSINESS_RULE_VIOLATION.message)

        return transitionTo(session, previousChapterFirstScene)
    }

    private fun transitionTo(session: SpeakingSession, scene: Scene): SpeakingSessionViewResult {
        val transitioned: SpeakingSession = session.transitionToScene(scene.sceneId.value, LocalDateTime.now(clock))
        commandSpeakingSessionPort.save(transitioned)
        val personalized: Scene = scene.personalizedFor(childAccessPort.findChildName(session.childId))
        val openingAudio: String? =
            personalized.characterOpening?.let { ttsPort.synthesize(it, personalized.characterVoice?.voiceProfile) }
        val closingAudio: String? =
            personalized.characterClosing?.let { ttsPort.synthesize(it, personalized.characterVoice?.voiceProfile) }
        val narrationAudio: String? = narrationAudio(personalized)
        val missionExplanationAudio: String? = missionExplanationAudio(personalized)

        return SpeakingSessionViewResult.scene(
            personalized,
            openingAudio,
            closingAudio,
            narrationAudio,
            missionExplanationAudio,
        )
    }

    private fun narrationAudio(scene: Scene): String? =
        if (scene.sceneType == SceneType.NARRATION) {
            ttsPort.synthesize(scene.sceneDescription, NARRATOR_VOICE_PROFILE)
        } else {
            null
        }

    private fun missionExplanationAudio(scene: Scene): String? =
        scene.mission?.explanationText()?.let { ttsPort.synthesize(it, NARRATOR_VOICE_PROFILE) }

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
