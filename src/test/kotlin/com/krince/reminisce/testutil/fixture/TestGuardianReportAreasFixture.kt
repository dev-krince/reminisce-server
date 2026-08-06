package com.krince.reminisce.testutil.fixture

import com.krince.reminisce.domain.model.report.CompetencyAnalysis
import com.krince.reminisce.domain.model.report.CompetencyItem
import com.krince.reminisce.domain.model.report.HomeConversationGuide
import com.krince.reminisce.domain.model.report.RepresentativeUtterance

object TestGuardianReportAreasFixture {
    private const val SAMPLE_LABEL: String = "역량명"
    private const val SAMPLE_FEATURE: String = "특징"
    private const val SAMPLE_STRENGTH: String = "잘한 점"
    private const val SAMPLE_IMPROVEMENT: String = "보완점"
    private const val SAMPLE_EVIDENCE: String = "근거 발화"
    private const val SAMPLE_REPRESENTATIVE_TEXT: String = "대표 발화"
    private const val SAMPLE_REPRESENTATIVE_REASON: String = "선정 이유"
    private const val SAMPLE_STORY_QUESTION: String = "이야기 주제 질문"
    private const val SAMPLE_DAILY_QUESTION: String = "일상 연결 질문"

    fun competencyItem(): CompetencyItem = CompetencyItem(
        label = SAMPLE_LABEL,
        feature = SAMPLE_FEATURE,
        evidenceUtterance = SAMPLE_EVIDENCE,
        strength = SAMPLE_STRENGTH,
        improvement = SAMPLE_IMPROVEMENT,
    )

    fun competencyAnalysis(): CompetencyAnalysis = CompetencyAnalysis(
        vocabulary = competencyItem(),
        perspectiveEmpathy = competencyItem(),
        emotion = competencyItem(),
        interaction = competencyItem(),
        thoughtReason = competencyItem(),
        resultSolution = competencyItem(),
    )

    fun representativeUtterance(): RepresentativeUtterance = RepresentativeUtterance(
        text = SAMPLE_REPRESENTATIVE_TEXT,
        reason = SAMPLE_REPRESENTATIVE_REASON,
    )

    fun homeConversationGuide(): HomeConversationGuide = HomeConversationGuide(
        storyThemeQuestions = listOf(SAMPLE_STORY_QUESTION),
        dailyLifeQuestions = listOf(SAMPLE_DAILY_QUESTION),
    )
}
