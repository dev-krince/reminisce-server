package com.krince.reminisce.domain.model.utteranceanalysis

import com.krince.reminisce.domain.model.message.vo.MessageId
import com.krince.reminisce.domain.model.utteranceanalysis.vo.AnalysisId
import com.krince.reminisce.domain.model.utteranceanalysis.vo.ChildIntent
import com.krince.reminisce.domain.model.utteranceanalysis.vo.UtteranceValidity
import com.krince.reminisce.shared.util.UuidGenerator

class UtteranceAnalysis(
    val analysisId: AnalysisId,
    val messageId: MessageId,
    val childIntent: ChildIntent,
    val mainPoint: String?,
    val detectedElements: List<DetectedElement>,
    val validity: UtteranceValidity,
) {
    companion object {
        fun of(
            messageId: MessageId,
            childIntent: ChildIntent,
            mainPoint: String?,
            detectedElements: List<DetectedElement>,
            validity: UtteranceValidity,
        ): UtteranceAnalysis = UtteranceAnalysis(
            analysisId = AnalysisId(UuidGenerator.generate()),
            messageId = messageId,
            childIntent = childIntent,
            mainPoint = mainPoint,
            detectedElements = detectedElements,
            validity = validity,
        )
    }
}
