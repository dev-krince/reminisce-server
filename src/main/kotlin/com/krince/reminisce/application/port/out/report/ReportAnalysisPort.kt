package com.krince.reminisce.application.port.out.report

import com.krince.reminisce.domain.model.report.HomeGuide
import com.krince.reminisce.domain.model.report.ParticipationItem
import com.krince.reminisce.domain.model.report.ReportOverall
import com.krince.reminisce.domain.model.report.ReportSpeechAnalysis
import com.krince.reminisce.domain.model.report.SceneHighlight
import com.krince.reminisce.domain.model.utteranceanalysis.DetectedElement

class ReportAnalysisContext(
    val childName: String?,
    val storyTitle: String,
    val scenes: List<ReportSceneContext>,
    val turns: List<ReportTurnContext>,
    val analyses: List<ReportUtteranceContext>,
)

class ReportSceneContext(
    val sceneId: String,
    val description: String,
    val goal: String?,
)

class ReportTurnContext(
    val sceneId: String,
    val turnOrder: Long,
    val isChild: Boolean,
    val text: String,
    val messageId: String?,
)

class ReportUtteranceContext(
    val messageId: String,
    val detectedElements: List<DetectedElement>,
)

class ReportAnalysisResult(
    val overall: ReportOverall,
    val participation: List<ParticipationItem>,
    val speechAnalyses: List<ReportSpeechAnalysis>,
    val sceneHighlights: List<SceneHighlight>,
    val representative: RepresentativeSelection,
    val homeGuide: HomeGuide,
)

data class RepresentativeSelection(
    val messageId: String?,
    val situation: String,
    val reason: String,
    val strengths: String,
    val practiceTip: String,
    val commentary: String,
    val chips: List<String>,
)

interface ReportAnalysisPort {
    fun analyze(context: ReportAnalysisContext): ReportAnalysisResult
}
