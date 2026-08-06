package com.krince.reminisce.infra.adapter.`in`.dto.report.response

import com.fasterxml.jackson.annotation.JsonFormat
import com.krince.reminisce.application.port.`in`.report.result.SessionReportResult
import com.krince.reminisce.domain.model.report.CompetencyAnalysis
import com.krince.reminisce.domain.model.report.CompetencyItem
import com.krince.reminisce.domain.model.report.HomeConversationGuide
import com.krince.reminisce.domain.model.report.RepresentativeUtterance
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(title = "SessionReportResponse", description = "말하기 세션 보호자 리포트 응답")
class SessionReportResponse(
    @field:Schema(description = "스텁 생성 요약문", required = true)
    val summary: String,

    @field:Schema(description = "세션에서 확인된 사고 요소 강점", example = "[\"EMOTION\", \"PERSPECTIVE\"]", required = true)
    val strengths: List<String>,

    @field:Schema(description = "아직 보여주지 않은 다음 초점 사고 요소", example = "[\"DECISION\", \"REASON\"]", required = true)
    val nextFocus: List<String>,

    @field:Schema(description = "말하기 역량 분석 (어휘·표현·논리)", required = true)
    val competencyAnalysis: CompetencyAnalysisResponse,

    @field:Schema(description = "대표 발화와 선정 이유", required = true)
    val representativeUtterance: RepresentativeUtteranceResponse,

    @field:Schema(description = "가정 연계 대화 가이드", required = true)
    val homeConversationGuide: HomeConversationGuideResponse,

    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @field:Schema(description = "리포트 생성 시각", example = "2026-01-09 14:30:25", required = true)
    val createdAt: LocalDateTime,
)

@Schema(title = "CompetencyItemResponse", description = "역량 항목")
class CompetencyItemResponse(
    @field:Schema(description = "역량명", required = true)
    val label: String,

    @field:Schema(description = "이번 활동 특징", required = true)
    val feature: String,

    @field:Schema(description = "근거가 된 아이 발화", required = false)
    val evidenceUtterance: String?,

    @field:Schema(description = "잘한 점", required = true)
    val strength: String,

    @field:Schema(description = "보완점", required = true)
    val improvement: String,
)

@Schema(title = "CompetencyAnalysisResponse", description = "역량 분석 (어휘·표현·논리)")
class CompetencyAnalysisResponse(
    @field:Schema(description = "어휘", required = true)
    val vocabulary: CompetencyItemResponse,

    @field:Schema(description = "표현 - 관점·공감", required = true)
    val perspectiveEmpathy: CompetencyItemResponse,

    @field:Schema(description = "표현 - 감정", required = true)
    val emotion: CompetencyItemResponse,

    @field:Schema(description = "표현 - 상호작용", required = true)
    val interaction: CompetencyItemResponse,

    @field:Schema(description = "논리 - 생각·이유", required = true)
    val thoughtReason: CompetencyItemResponse,

    @field:Schema(description = "논리 - 결과·해결", required = true)
    val resultSolution: CompetencyItemResponse,
)

@Schema(title = "RepresentativeUtteranceResponse", description = "대표 발화")
class RepresentativeUtteranceResponse(
    @field:Schema(description = "대표로 선정된 아이 발화", required = false)
    val text: String?,

    @field:Schema(description = "선정 이유", required = true)
    val reason: String,
)

@Schema(title = "HomeConversationGuideResponse", description = "가정 연계 대화 가이드")
class HomeConversationGuideResponse(
    @field:Schema(description = "이야기 주제 이어가기 질문", required = true)
    val storyThemeQuestions: List<String>,

    @field:Schema(description = "일상생활 연결 질문", required = true)
    val dailyLifeQuestions: List<String>,
)

fun sessionReportResponse(result: SessionReportResult): SessionReportResponse = SessionReportResponse(
    summary = result.summary,
    strengths = result.strengths.map { it.name },
    nextFocus = result.nextFocus.map { it.name },
    competencyAnalysis = competencyAnalysisResponse(result.competencyAnalysis),
    representativeUtterance = representativeUtteranceResponse(result.representativeUtterance),
    homeConversationGuide = homeConversationGuideResponse(result.homeConversationGuide),
    createdAt = result.createdAt,
)

private fun competencyAnalysisResponse(analysis: CompetencyAnalysis): CompetencyAnalysisResponse =
    CompetencyAnalysisResponse(
        vocabulary = competencyItemResponse(analysis.vocabulary),
        perspectiveEmpathy = competencyItemResponse(analysis.perspectiveEmpathy),
        emotion = competencyItemResponse(analysis.emotion),
        interaction = competencyItemResponse(analysis.interaction),
        thoughtReason = competencyItemResponse(analysis.thoughtReason),
        resultSolution = competencyItemResponse(analysis.resultSolution),
    )

private fun competencyItemResponse(item: CompetencyItem): CompetencyItemResponse = CompetencyItemResponse(
    label = item.label,
    feature = item.feature,
    evidenceUtterance = item.evidenceUtterance,
    strength = item.strength,
    improvement = item.improvement,
)

private fun representativeUtteranceResponse(utterance: RepresentativeUtterance): RepresentativeUtteranceResponse =
    RepresentativeUtteranceResponse(
        text = utterance.text,
        reason = utterance.reason,
    )

private fun homeConversationGuideResponse(guide: HomeConversationGuide): HomeConversationGuideResponse =
    HomeConversationGuideResponse(
        storyThemeQuestions = guide.storyThemeQuestions,
        dailyLifeQuestions = guide.dailyLifeQuestions,
    )
