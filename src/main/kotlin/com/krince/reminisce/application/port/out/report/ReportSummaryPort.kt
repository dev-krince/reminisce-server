package com.krince.reminisce.application.port.out.report

import com.krince.reminisce.domain.model.story.vo.ThinkingElement

interface ReportSummaryPort {
    fun generate(strengths: List<ThinkingElement>, nextFocus: List<ThinkingElement>): String
}
