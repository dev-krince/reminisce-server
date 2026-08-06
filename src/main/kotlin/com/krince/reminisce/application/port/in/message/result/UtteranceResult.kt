package com.krince.reminisce.application.port.`in`.message.result

import com.krince.reminisce.domain.model.message.Message
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
) {
    class DetectedElementResult(
        val type: String,
        val evidence: String,
    )

    companion object {
        fun from(
            message: Message,
            analysis: UtteranceAnalysis,
            accumulatedElements: List<ThinkingElement>,
            missingElements: List<ThinkingElement>,
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
            accumulatedElements = accumulatedElements.map { it.name },
            missingElements = missingElements.map { it.name },
        )
    }
}
