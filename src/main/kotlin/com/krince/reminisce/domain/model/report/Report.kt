package com.krince.reminisce.domain.model.report

import com.krince.reminisce.domain.model.report.vo.ReportId
import com.krince.reminisce.domain.model.speakingsession.vo.SpeakingSessionId
import com.krince.reminisce.domain.model.story.vo.ThinkingElement
import com.krince.reminisce.shared.util.UuidGenerator
import java.time.LocalDateTime

data class Report(
    val reportId: ReportId,
    val sessionId: SpeakingSessionId,
    val summary: String,
    val strengths: List<ThinkingElement>,
    val nextFocus: List<ThinkingElement>,
    val competencyAnalysis: CompetencyAnalysis,
    val representativeUtterance: RepresentativeUtterance,
    val homeConversationGuide: HomeConversationGuide,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun generate(
            sessionId: SpeakingSessionId,
            strengths: List<ThinkingElement>,
            nextFocus: List<ThinkingElement>,
            summary: String,
            competencyAnalysis: CompetencyAnalysis,
            representativeUtterance: RepresentativeUtterance,
            homeConversationGuide: HomeConversationGuide,
            at: LocalDateTime,
        ): Report = Report(
            reportId = ReportId(UuidGenerator.generate()),
            sessionId = sessionId,
            summary = summary,
            strengths = strengths,
            nextFocus = nextFocus,
            competencyAnalysis = competencyAnalysis,
            representativeUtterance = representativeUtterance,
            homeConversationGuide = homeConversationGuide,
            createdAt = at,
        )
    }
}
