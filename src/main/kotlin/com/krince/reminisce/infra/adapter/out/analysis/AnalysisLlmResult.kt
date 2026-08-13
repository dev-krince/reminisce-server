package com.krince.reminisce.infra.adapter.out.analysis

import com.krince.reminisce.domain.model.story.vo.ThinkingElement
import com.krince.reminisce.domain.model.utteranceanalysis.DetectedElement
import com.krince.reminisce.domain.model.utteranceanalysis.RawUtteranceAnalysis
import com.krince.reminisce.domain.model.utteranceanalysis.vo.ChildIntent
import com.krince.reminisce.domain.model.utteranceanalysis.vo.UtteranceValidity

data class AnalysisLlmResult(
    val childIntent: String? = null,
    val mainPoint: String? = null,
    val detectedElements: List<DetectedElementLlm> = emptyList(),
    val validity: String? = null,
) {
    fun toRawUtteranceAnalysis(): RawUtteranceAnalysis =
        RawUtteranceAnalysis(
            childIntent = enumOrDefault(childIntent, ChildIntent.UNCLEAR),
            mainPoint = mainPoint?.trim()?.takeIf { it.isNotBlank() },
            detectedElements = detectedElements.mapNotNull { it.toDomainOrNull() },
            validity = enumOrDefault(validity, UtteranceValidity.UNCLEAR),
        )
}

data class DetectedElementLlm(
    val type: String? = null,
    val evidence: String? = null,
) {
    fun toDomainOrNull(): DetectedElement? {
        val element: ThinkingElement = enumOrNull<ThinkingElement>(type) ?: return null
        val cleanEvidence: String = evidence?.trim()?.takeIf { it.isNotBlank() } ?: return null

        return DetectedElement(type = element, evidence = cleanEvidence)
    }
}

internal inline fun <reified T : Enum<T>> enumOrNull(value: String?): T? {
    val normalized: String = value?.trim()?.uppercase()?.replace(' ', '_') ?: return null

    return enumValues<T>().firstOrNull { it.name == normalized }
}

internal inline fun <reified T : Enum<T>> enumOrDefault(value: String?, default: T): T =
    enumOrNull<T>(value) ?: default
