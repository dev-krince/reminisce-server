package com.krince.reminisce.application.port.`in`.report.result

import com.krince.reminisce.domain.model.report.HomeGuide
import com.krince.reminisce.domain.model.report.ParticipationItem
import com.krince.reminisce.domain.model.report.ReportOverall
import com.krince.reminisce.domain.model.report.ReportSpeechAnalysis
import java.time.LocalDateTime

data class SessionReportResult(
    val summary: SessionReportSummary,
    val overall: ReportOverall,
    val participation: List<ParticipationItem>,
    val speechAnalyses: List<ReportSpeechAnalysis>,
    val sceneCards: List<SessionReportSceneCard>,
    val representative: SessionReportRepresentative,
    val homeGuide: HomeGuide,
    val createdAt: LocalDateTime,
)

data class SessionReportSummary(
    val childName: String?,
    val storyTitle: String,
    val storyImageUrl: String?,
    val activityDate: LocalDateTime,
    val durationMinutes: Long,
    val cardOrderCompleted: Boolean,
    val retellingCompleted: Boolean,
)

data class SessionReportSceneCard(
    val sceneNumber: Int,
    val sceneId: String,
    val title: String?,
    val imageUrl: String?,
    val situation: String,
    val characterQuestion: String?,
    val childUtterance: SessionReportChildUtterance,
    val featureSentence: String,
    val featureChips: List<String>,
)

data class SessionReportChildUtterance(
    val text: String,
    val audioUrl: String?,
    val sttRawText: String?,
)

data class SessionReportRepresentative(
    val text: String?,
    val audioUrl: String?,
    val commentary: String,
    val chips: List<String>,
    val situation: String,
    val reason: String,
    val strengths: String,
    val practiceTip: String,
)
