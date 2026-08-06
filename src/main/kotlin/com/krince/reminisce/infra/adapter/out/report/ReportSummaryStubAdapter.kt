package com.krince.reminisce.infra.adapter.out.report

import com.krince.reminisce.application.port.out.report.ReportSummaryPort
import com.krince.reminisce.domain.model.story.vo.ThinkingElement
import org.springframework.stereotype.Component

@Component
class ReportSummaryStubAdapter : ReportSummaryPort {

    override fun generate(strengths: List<ThinkingElement>, nextFocus: List<ThinkingElement>): String {
        val strengthsPart: String = describe(STRENGTHS_PREFIX, strengths, NO_STRENGTHS)
        val nextFocusPart: String = describe(NEXT_FOCUS_PREFIX, nextFocus, NO_NEXT_FOCUS)

        return "$strengthsPart$SENTENCE_SEPARATOR$nextFocusPart"
    }

    private fun describe(prefix: String, elements: List<ThinkingElement>, emptyMessage: String): String {
        if (elements.isEmpty()) {
            return "$prefix$emptyMessage"
        }

        return "$prefix${elements.joinToString(ELEMENT_SEPARATOR) { it.name }}"
    }

    companion object {
        private const val STRENGTHS_PREFIX: String = "이번 세션에서 확인된 강점: "
        private const val NEXT_FOCUS_PREFIX: String = "다음에 함께 살펴보면 좋은 사고 요소: "
        private const val NO_STRENGTHS: String = "아직 확인된 사고 요소가 없어요."
        private const val NO_NEXT_FOCUS: String = "모든 사고 요소를 보여주었어요."
        private const val ELEMENT_SEPARATOR: String = ", "
        private const val SENTENCE_SEPARATOR: String = " "
    }
}
