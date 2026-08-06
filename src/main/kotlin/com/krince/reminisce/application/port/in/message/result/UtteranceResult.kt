package com.krince.reminisce.application.port.`in`.message.result

import com.krince.reminisce.domain.model.message.Message
import com.krince.reminisce.domain.model.speakingsession.SpeakingSession
import com.krince.reminisce.domain.model.story.vo.ThinkingElement
import com.krince.reminisce.domain.model.utteranceanalysis.UtteranceAnalysis
import java.time.LocalDateTime

class UtteranceResult(
    val messageId: String,
    val sceneId: String,
    val speakerType: String,
    val turnOrder: Long,
    val text: String,
    val createdAt: LocalDateTime,
    val childIntent: String,
    val mainPoint: String?,
    val validity: String,
    val detectedElements: List<DetectedElementResult>,
    val accumulatedElements: List<String>,
    val missingElements: List<String>,
    val mode: String,
    val sceneEndReason: String?,
    val sceneGoalMet: Boolean,
    val guidanceTarget: String?,
    val characterReply: CharacterReplyResult,
) {
    class DetectedElementResult(
        val type: String,
        val evidence: String,
    )

    class CharacterReplyResult(
        val messageId: String,
        val speakerType: String,
        val turnOrder: Long,
        val text: String,
        val audio: String?,
    ) {
        companion object {
            fun from(message: Message, audio: String?): CharacterReplyResult = CharacterReplyResult(
                messageId = message.messageId.value,
                speakerType = message.speakerType.name,
                turnOrder = message.turnOrder,
                text = message.text,
                audio = audio,
            )
        }
    }

    companion object {
        fun from(
            message: Message,
            analysis: UtteranceAnalysis,
            session: SpeakingSession,
            missingElements: List<ThinkingElement>,
            characterMessage: Message,
            characterReplyAudio: String?,
        ): UtteranceResult = UtteranceResult(
            messageId = message.messageId.value,
            sceneId = message.sceneId.value,
            speakerType = message.speakerType.name,
            turnOrder = message.turnOrder,
            text = message.text,
            createdAt = message.createdAt,
            childIntent = analysis.childIntent.name,
            mainPoint = analysis.mainPoint,
            validity = analysis.validity.name,
            detectedElements = analysis.detectedElements.map {
                DetectedElementResult(type = it.type.name, evidence = it.evidence)
            },
            accumulatedElements = session.accumulatedElements.map { it.name },
            missingElements = missingElements.map { it.name },
            mode = requireNotNull(session.lastResponseMode).name,
            sceneEndReason = session.sceneEndReason?.name,
            sceneGoalMet = session.sceneGoalMet,
            guidanceTarget = session.lastGuidanceTarget?.name,
            characterReply = CharacterReplyResult.from(characterMessage, characterReplyAudio),
        )
    }
}
