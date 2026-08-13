package com.krince.reminisce.infra.adapter.out.analysis

import com.krince.reminisce.application.port.out.analysis.SpeechAnalysisPort
import com.krince.reminisce.domain.model.story.vo.ThinkingElement
import com.krince.reminisce.domain.model.utteranceanalysis.DetectedElement
import com.krince.reminisce.domain.model.utteranceanalysis.RawUtteranceAnalysis
import com.krince.reminisce.domain.model.utteranceanalysis.vo.ChildIntent
import com.krince.reminisce.domain.model.utteranceanalysis.vo.UtteranceValidity
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(name = ["analysis.engine"], havingValue = "stub", matchIfMissing = true)
class SpeechAnalysisStubAdapter : SpeechAnalysisPort {

    override fun analyze(text: String): RawUtteranceAnalysis {
        val detectedElements: List<DetectedElement> = detectElements(text)

        return RawUtteranceAnalysis(
            childIntent = resolveIntent(detectedElements),
            mainPoint = resolveMainPoint(text),
            detectedElements = detectedElements,
            validity = resolveValidity(text),
        )
    }

    private fun detectElements(text: String): List<DetectedElement> =
        ELEMENT_KEYWORDS
            .mapNotNull { (element, keywords) -> matchElement(text, element, keywords) }

    private fun matchElement(
        text: String,
        element: ThinkingElement,
        keywords: List<String>,
    ): DetectedElement? {
        val matched: String = keywords.firstOrNull { text.contains(it) } ?: return null

        return DetectedElement(type = element, evidence = matched)
    }

    private fun resolveIntent(detectedElements: List<DetectedElement>): ChildIntent {
        val leadingElement: ThinkingElement = detectedElements.firstOrNull()?.type
            ?: return ChildIntent.SHORT_RESPONSE

        return INTENT_BY_ELEMENT[leadingElement] ?: ChildIntent.OPINION
    }

    private fun resolveMainPoint(text: String): String? {
        val trimmed: String = text.trim()
        if (trimmed.isBlank()) {
            return null
        }

        return trimmed
    }

    private fun resolveValidity(text: String): UtteranceValidity {
        if (text.trim().length < MIN_VALID_LENGTH) {
            return UtteranceValidity.SHORT
        }

        return UtteranceValidity.VALID
    }

    companion object {
        private const val MIN_VALID_LENGTH: Int = 4

        private val ELEMENT_KEYWORDS: Map<ThinkingElement, List<String>> = mapOf(
            ThinkingElement.EMOTION to listOf("힘들", "슬프", "속상", "무서"),
            ThinkingElement.EMPATHY to listOf("이해", "공감", "마음"),
            ThinkingElement.PERSPECTIVE to listOf("입장", "생각하면"),
            ThinkingElement.REASON to listOf("니까", "때문", "라서"),
            ThinkingElement.SOLUTION to listOf("하면 돼", "방법", "이렇게"),
            ThinkingElement.DECISION to listOf("할래", "할게", "하겠"),
            ThinkingElement.RESULT to listOf("결국", "그래서"),
            ThinkingElement.REQUEST to listOf("주세요", "해줘", "부탁"),
        )

        private val INTENT_BY_ELEMENT: Map<ThinkingElement, ChildIntent> = mapOf(
            ThinkingElement.EMOTION to ChildIntent.EMOTION,
            ThinkingElement.EMPATHY to ChildIntent.PERSPECTIVE,
            ThinkingElement.PERSPECTIVE to ChildIntent.PERSPECTIVE,
            ThinkingElement.REASON to ChildIntent.REASONING,
            ThinkingElement.SOLUTION to ChildIntent.SOLUTION,
            ThinkingElement.DECISION to ChildIntent.DECISION,
            ThinkingElement.RESULT to ChildIntent.REASONING,
            ThinkingElement.REQUEST to ChildIntent.REQUEST,
        )
    }
}
