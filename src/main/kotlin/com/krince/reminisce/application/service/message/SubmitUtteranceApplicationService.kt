package com.krince.reminisce.application.service.message

import com.krince.reminisce.application.port.access.child.ChildAccessPort
import com.krince.reminisce.application.port.access.story.StoryAccessPort
import com.krince.reminisce.application.port.`in`.message.command.SubmitUtteranceCommand
import com.krince.reminisce.application.port.`in`.message.result.UtteranceResult
import com.krince.reminisce.application.port.`in`.message.usecase.SubmitUtteranceUseCase
import com.krince.reminisce.application.port.out.analysis.SpeechAnalysisPort
import com.krince.reminisce.application.port.out.message.CommandMessagePort
import com.krince.reminisce.application.port.out.message.LoadMessagePort
import com.krince.reminisce.application.port.out.conversation.ConversationTurn
import com.krince.reminisce.application.port.out.reply.CharacterReplyContext
import com.krince.reminisce.application.port.out.reply.CharacterReplyPort
import com.krince.reminisce.application.port.out.speakingsession.CommandSpeakingSessionPort
import com.krince.reminisce.application.port.out.speakingsession.LoadSpeakingSessionPort
import com.krince.reminisce.application.port.out.tts.TtsPort
import com.krince.reminisce.application.port.out.utteranceanalysis.CommandUtteranceAnalysisPort
import com.krince.reminisce.domain.model.message.Message
import com.krince.reminisce.domain.model.message.vo.SpeakerType
import com.krince.reminisce.domain.model.speakingsession.SpeakingSession
import com.krince.reminisce.domain.model.speakingsession.vo.ResponseMode
import com.krince.reminisce.domain.model.speakingsession.vo.SessionStatus
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
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDateTime

@Service
class SubmitUtteranceApplicationService(
    private val loadSpeakingSessionPort: LoadSpeakingSessionPort,
    private val childAccessPort: ChildAccessPort,
    private val storyAccessPort: StoryAccessPort,
    private val ttsPort: TtsPort,
    private val commandMessagePort: CommandMessagePort,
    private val loadMessagePort: LoadMessagePort,
    private val speechAnalysisPort: SpeechAnalysisPort,
    private val commandUtteranceAnalysisPort: CommandUtteranceAnalysisPort,
    private val commandSpeakingSessionPort: CommandSpeakingSessionPort,
    private val characterReplyPort: CharacterReplyPort,
    private val clock: Clock,
) : SubmitUtteranceUseCase {

    private val firstTurnOffset: Long = 1L
    private val nextTurnOffset: Long = 1L
    private val recentTurnLimit: Int = 6

    @Transactional
    override fun execute(command: SubmitUtteranceCommand): UtteranceResult {
        val session: SpeakingSession = loadOwnedSession(command.sessionId, command.guardianId)
        if (session.status != SessionStatus.IN_PROGRESS) {
            throw BusinessRuleViolationException(BUSINESS_RULE_VIOLATION, BUSINESS_RULE_VIOLATION.message)
        }
        if (session.sceneEndReason != null) {
            throw BusinessRuleViolationException(BUSINESS_RULE_VIOLATION, BUSINESS_RULE_VIOLATION.message)
        }
        val childName: String? = childAccessPort.findChildName(session.childId)
        val dialogueScene: Scene = requireDialogueScene(session).personalizedFor(childName)
        val message: Message = saveChildUtterance(session, dialogueScene, command.text, command.sttRawText)
        val recentTurns: List<ConversationTurn> = loadRecentTurns(session, message)
        val analysis: UtteranceAnalysis = analyzeAndSave(message, recentTurns)
        val progressedSession: SpeakingSession = progressAndSave(session, analysis, dialogueScene)
        val missingElements: List<ThinkingElement> = missingElements(dialogueScene, progressedSession)
        val characterMessage: Message = saveCharacterReply(message, dialogueScene, progressedSession, childName, recentTurns)
        val characterReplyAudio: String? = ttsPort.synthesize(characterMessage.text, dialogueScene.characterVoice?.voiceProfile)

        return UtteranceResult.from(message, analysis, progressedSession, missingElements, characterMessage, characterReplyAudio)
    }

    private fun saveCharacterReply(
        childMessage: Message,
        scene: Scene,
        session: SpeakingSession,
        childName: String?,
        recentTurns: List<ConversationTurn>,
    ): Message {
        val replyText: String = characterReplyText(childMessage, scene, session, childName, recentTurns)
        val characterMessage: Message = Message.characterReply(
            sessionId = session.sessionId,
            sceneId = SceneId(scene.sceneId.value),
            turnOrder = childMessage.turnOrder + nextTurnOffset,
            text = replyText,
            at = LocalDateTime.now(clock),
        )

        return commandMessagePort.save(characterMessage)
    }

    private fun characterReplyText(
        childMessage: Message,
        scene: Scene,
        session: SpeakingSession,
        childName: String?,
        recentTurns: List<ConversationTurn>,
    ): String {
        val mode: ResponseMode = requireNotNull(session.lastResponseMode)
        if (mode == ResponseMode.CLOSING) {
            return checkNotNull(scene.characterClosing)
        }

        return characterReplyPort.generate(
            CharacterReplyContext(
                characterDisplayName = checkNotNull(scene.characterDisplayName),
                mode = mode,
                childUtterance = childMessage.text,
                guidanceTarget = session.lastGuidanceTarget,
                characterOpening = scene.characterOpening,
                conflict = scene.conflict,
                sceneGoal = scene.sceneGoal,
                childName = childName,
                recentTurns = recentTurns,
            ),
        )
    }

    private fun loadRecentTurns(session: SpeakingSession, currentChildMessage: Message): List<ConversationTurn> =
        loadMessagePort.findRecentMessagesBySession(session.sessionId, recentTurnLimit)
            .filter { it.messageId != currentChildMessage.messageId }
            .map { ConversationTurn(isChild = it.speakerType == SpeakerType.CHILD, text = it.text) }

    private fun analyzeAndSave(message: Message, recentTurns: List<ConversationTurn>): UtteranceAnalysis {
        val raw: RawUtteranceAnalysis = speechAnalysisPort.analyze(message.text, recentTurns)
        val analysis: UtteranceAnalysis = raw.verifyAgainst(message.text, message.messageId)

        return commandUtteranceAnalysisPort.save(analysis)
    }

    private fun progressAndSave(
        session: SpeakingSession,
        analysis: UtteranceAnalysis,
        scene: Scene,
    ): SpeakingSession {
        val newTypes: List<ThinkingElement> = analysis.detectedElements.map { it.type }
        val at: LocalDateTime = LocalDateTime.now(clock)
        val accumulatedSession: SpeakingSession = session.accumulate(newTypes, at)
        val hasNewElement: Boolean =
            accumulatedSession.accumulatedElements.size > session.accumulatedElements.size
        val missingElements: List<ThinkingElement> = missingElements(scene, accumulatedSession)
        val progressedSession: SpeakingSession = accumulatedSession.advanceTurn(
            hasNewElement = hasNewElement,
            validity = analysis.validity,
            missingElements = missingElements,
            preferredTurns = scene.preferredTurns,
            maxTurns = requiredMaxTurns(scene),
            at = at,
        )

        return commandSpeakingSessionPort.save(progressedSession)
    }

    private fun requiredMaxTurns(scene: Scene): Int =
        scene.maxTurns ?: throw BusinessRuleViolationException(BUSINESS_RULE_VIOLATION, BUSINESS_RULE_VIOLATION.message)

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

    private fun saveChildUtterance(
        session: SpeakingSession,
        scene: Scene,
        text: String,
        sttRawText: String?,
    ): Message {
        val turnOrder: Long = loadMessagePort.countBySession(session.sessionId) + firstTurnOffset
        val message: Message = Message.childUtterance(
            sessionId = session.sessionId,
            sceneId = SceneId(scene.sceneId.value),
            turnOrder = turnOrder,
            text = text,
            sttRawText = sttRawText,
            at = LocalDateTime.now(clock),
        )

        return commandMessagePort.save(message)
    }
}
