package com.krince.reminisce.domain.model.report

import com.krince.reminisce.domain.model.report.vo.ReportId
import com.krince.reminisce.domain.model.speakingsession.vo.SpeakingSessionId
import com.krince.reminisce.shared.util.UuidGenerator
import java.time.LocalDateTime

data class Report(
    val reportId: ReportId,
    val sessionId: SpeakingSessionId,
    val overall: ReportOverall,
    val participation: List<ParticipationItem>,
    val speechAnalyses: List<ReportSpeechAnalysis>,
    val sceneHighlights: List<SceneHighlight>,
    val representative: RepresentativeUtterance,
    val homeGuide: HomeGuide,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun generate(
            sessionId: SpeakingSessionId,
            overall: ReportOverall,
            participation: List<ParticipationItem>,
            speechAnalyses: List<ReportSpeechAnalysis>,
            sceneHighlights: List<SceneHighlight>,
            representative: RepresentativeUtterance,
            homeGuide: HomeGuide,
            at: LocalDateTime,
        ): Report = Report(
            reportId = ReportId(UuidGenerator.generate()),
            sessionId = sessionId,
            overall = overall,
            participation = participation,
            speechAnalyses = speechAnalyses,
            sceneHighlights = sceneHighlights,
            representative = representative,
            homeGuide = homeGuide,
            createdAt = at,
        )
    }
}
