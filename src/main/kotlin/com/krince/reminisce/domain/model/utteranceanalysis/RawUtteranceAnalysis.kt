package com.krince.reminisce.domain.model.utteranceanalysis

import com.krince.reminisce.domain.model.message.vo.MessageId
import com.krince.reminisce.domain.model.utteranceanalysis.vo.ChildIntent
import com.krince.reminisce.domain.model.utteranceanalysis.vo.UtteranceValidity

class RawUtteranceAnalysis(
    val childIntent: ChildIntent,
    val mainPoint: String?,
    val detectedElements: List<DetectedElement>,
    val validity: UtteranceValidity,
) {
    fun verifyAgainst(text: String, messageId: MessageId): UtteranceAnalysis {
        val normalizedText: String = normalize(text)
        val verifiedElements: List<DetectedElement> = detectedElements
            .filter { isEvidenceSupported(it.evidence, normalizedText) }

        return UtteranceAnalysis.of(
            messageId = messageId,
            childIntent = childIntent,
            mainPoint = mainPoint,
            detectedElements = verifiedElements,
            validity = validity,
        )
    }

    private fun isEvidenceSupported(evidence: String, normalizedText: String): Boolean {
        val normalizedEvidence: String = normalize(evidence)
        if (normalizedEvidence.isBlank()) {
            return false
        }

        return normalizedText.contains(normalizedEvidence)
    }

    private fun normalize(value: String): String =
        value.trim().replace(WHITESPACE_RUN, SINGLE_SPACE)

    companion object {
        private val WHITESPACE_RUN: Regex = Regex("\\s+")
        private const val SINGLE_SPACE: String = " "
    }
}
