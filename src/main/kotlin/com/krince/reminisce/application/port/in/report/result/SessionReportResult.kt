package com.krince.reminisce.application.port.`in`.report.result

import com.krince.reminisce.domain.model.report.CompetencyAnalysis
import com.krince.reminisce.domain.model.report.HomeConversationGuide
import com.krince.reminisce.domain.model.report.RepresentativeUtterance
import com.krince.reminisce.domain.model.story.vo.ThinkingElement
import java.time.LocalDateTime

data class SessionReportResult(
    val summary: String,
    val strengths: List<ThinkingElement>,
    val nextFocus: List<ThinkingElement>,
    val competencyAnalysis: CompetencyAnalysis,
    val representativeUtterance: RepresentativeUtterance,
    val homeConversationGuide: HomeConversationGuide,
    val createdAt: LocalDateTime,
)
