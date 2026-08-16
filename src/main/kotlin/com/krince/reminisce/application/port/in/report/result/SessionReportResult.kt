package com.krince.reminisce.application.port.`in`.report.result

import com.krince.reminisce.domain.model.report.HomeGuide
import com.krince.reminisce.domain.model.report.ParticipationItem
import com.krince.reminisce.domain.model.report.ReportOverall
import com.krince.reminisce.domain.model.report.ReportSpeechAnalysis
import com.krince.reminisce.domain.model.report.RepresentativeUtterance
import com.krince.reminisce.domain.model.report.SceneHighlight
import java.time.LocalDateTime

data class SessionReportResult(
    val overall: ReportOverall,
    val participation: List<ParticipationItem>,
    val speechAnalyses: List<ReportSpeechAnalysis>,
    val sceneHighlights: List<SceneHighlight>,
    val representative: RepresentativeUtterance,
    val homeGuide: HomeGuide,
    val createdAt: LocalDateTime,
)
