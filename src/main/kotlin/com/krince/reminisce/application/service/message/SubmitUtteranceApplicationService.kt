package com.krince.reminisce.application.service.message

import com.krince.reminisce.application.port.access.child.ChildAccessPort
import com.krince.reminisce.application.port.access.story.StoryAccessPort
import com.krince.reminisce.application.port.`in`.message.command.SubmitUtteranceCommand
import com.krince.reminisce.application.port.`in`.message.result.UtteranceResult
import com.krince.reminisce.application.port.`in`.message.usecase.SubmitUtteranceUseCase
import com.krince.reminisce.application.port.out.analysis.SpeechAnalysisPort
import com.krince.reminisce.application.port.out.message.CommandMessagePort
import com.krince.reminisce.application.port.out.message.LoadMessagePort
import com.krince.reminisce.application.port.out.speakingsession.CommandSpeakingSessionPort
import com.krince.reminisce.application.port.out.speakingsession.LoadSpeakingSessionPort
import com.krince.reminisce.application.port.out.stt.SttPort
import com.krince.reminisce.application.port.out.utteranceanalysis.CommandUtteranceAnalysisPort
import com.krince.reminisce.domain.model.message.Message
import com.krince.reminisce.domain.model.speakingsession.SpeakingSession
import com.krince.reminisce.domain.model.speakingsession.vo.SpeakingSessionId
import com.krince.reminisce.domain.model.story.Scene
import com.krince.reminisce.domain.model.story.vo.SceneId
import com.krince.reminisce.domain.model.story.vo.SceneType
import com.krince.reminisce.domain.model.story.vo.ThinkingElement
import com.krince.reminisce.domain.model.user.vo.UserId
import com.krince.reminisce.domain.model.utteranceanalysis.RawUtteranceAnalysis
import com.krince.reminisce.domain.model.utteranceanalysis.UtteranceAnalysis
import com.krince.reminisce.shared.exception.BusinessRuleViolationException
import com.krince.reminisce.shared.exception.NotFoundException
import com.krince.reminisce.shared.response.ExceptionResponseCode.BUSINESS_RULE_VIOLATION
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND
import com.krince.reminisce.shared.response.ExceptionResponseCode.STT_TRANSCRIPTION_FAILED
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDateTime

@Service
class SubmitUtteranceApplicationService(
    private val loadSpeakingSessionPort: LoadSpeakingSessionPort,
    private val childAccessPort: ChildAccessPort,
    private val storyAccessPort: StoryAccessPort,
    private val sttPort: SttPort,
    private val commandMessagePort: CommandMessagePort,
    private val loadMessagePort: LoadMessagePort,
    private val speechAnalysisPort: SpeechAnalysisPort,
    private val commandUtteranceAnalysisPort: CommandUtteranceAnalysisPort,
    private val commandSpeakingSessionPort: CommandSpeakingSessionPort,
    private val clock: Clock,
) : SubmitUtteranceUseCase {

    private val firstTurnOffset: Long = 1L

    @Transactional
    override fun execute(command: SubmitUtteranceCommand): UtteranceResult {
        val session: SpeakingSession = loadOwnedSession(command.sessionId, command.guardianId)
        val dialogueScene: Scene = requireDialogueScene(session)
        val transcript: String = transcribe(command.audio)
        val message: Message = saveChildUtterance(session, dialogueScene, transcript)
        val analysis: UtteranceAnalysis = analyzeAndSave(message)
        val updatedSession: SpeakingSession = accumulateAndSave(session, analysis)
        val missingElements: List<ThinkingElement> = missingElements(dialogueScene, updatedSession)

        return UtteranceResult.from(message, analysis, updatedSession.accumulatedElements, missingElements)
    }

    private fun analyzeAndSave(message: Message): UtteranceAnalysis {
        val raw: RawUtteranceAnalysis = speechAnalysisPort.analyze(message.text)
        val analysis: UtteranceAnalysis = raw.verifyAgainst(message.text, message.messageId)

        return commandUtteranceAnalysisPort.save(analysis)
    }

    private fun accumulateAndSave(session: SpeakingSession, analysis: UtteranceAnalysis): SpeakingSession {
        val newTypes: List<ThinkingElement> = analysis.detectedElements.map { it.type }
        val updatedSession: SpeakingSession = session.accumulate(newTypes, LocalDateTime.now(clock))

        return commandSpeakingSessionPort.save(updatedSession)
    }

    private fun missingElements(scene: Scene, session: SpeakingSession): List<ThinkingElement> =
        (scene.requiredElements ?: emptyList()) - session.accumulatedElements.toSet()

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

    private fun requireDialogueScene(session: SpeakingSession): Scene {
        val currentSceneId: String = session.currentSceneId
            ?: throw BusinessRuleViolationException(BUSINESS_RULE_VIOLATION, BUSINESS_RULE_VIOLATION.message)
        val scene: Scene = storyAccessPort.findScene(session.storyId, currentSceneId)
            ?: throw BusinessRuleViolationException(BUSINESS_RULE_VIOLATION, BUSINESS_RULE_VIOLATION.message)
        if (scene.sceneType != SceneType.DIALOGUE) {
            throw BusinessRuleViolationException(BUSINESS_RULE_VIOLATION, BUSINESS_RULE_VIOLATION.message)
        }

        return scene
    }

    private fun transcribe(audio: String): String =
        sttPort.transcribe(audio)
            ?: throw BusinessRuleViolationException(STT_TRANSCRIPTION_FAILED, STT_TRANSCRIPTION_FAILED.message)

    private fun saveChildUtterance(session: SpeakingSession, scene: Scene, transcript: String): Message {
        val turnOrder: Long = loadMessagePort.countBySession(session.sessionId) + firstTurnOffset
        val message: Message = Message.childUtterance(
            sessionId = session.sessionId,
            sceneId = SceneId(scene.sceneId.value),
            turnOrder = turnOrder,
            text = transcript,
            sttRawText = transcript,
            at = LocalDateTime.now(clock),
        )

        return commandMessagePort.save(message)
    }
}
