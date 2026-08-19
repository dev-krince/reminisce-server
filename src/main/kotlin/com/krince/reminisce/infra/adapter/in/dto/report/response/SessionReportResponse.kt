package com.krince.reminisce.infra.adapter.`in`.dto.report.response

import com.fasterxml.jackson.annotation.JsonFormat
import com.krince.reminisce.application.port.`in`.report.result.SessionReportChildUtterance
import com.krince.reminisce.application.port.`in`.report.result.SessionReportRepresentative
import com.krince.reminisce.application.port.`in`.report.result.SessionReportResult
import com.krince.reminisce.application.port.`in`.report.result.SessionReportSceneCard
import com.krince.reminisce.application.port.`in`.report.result.SessionReportSummary
import com.krince.reminisce.domain.model.report.GuideDirection
import com.krince.reminisce.domain.model.report.GuideQuestion
import com.krince.reminisce.domain.model.report.HomeGuide
import com.krince.reminisce.domain.model.report.ParticipationItem
import com.krince.reminisce.domain.model.report.ReportOverall
import com.krince.reminisce.domain.model.report.ReportSpeechAnalysis
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(title = "SessionReportResponse", description = "말하기 세션 보호자 리포트 응답 (5개 탭)")
class SessionReportResponse(
    @field:Schema(description = "총합 요약 탭", required = true)
    val summaryTab: SummaryTabResponse,

    @field:Schema(description = "어휘·표현·논리 탭", required = true)
    val speechTab: SpeechTabResponse,

    @field:Schema(description = "장면별 발화 탭", required = true)
    val sceneTab: SceneTabResponse,

    @field:Schema(description = "대표 발화 탭", required = true)
    val representativeTab: RepresentativeTabResponse,

    @field:Schema(description = "가정 대화 가이드 탭", required = true)
    val homeGuideTab: HomeGuideTabResponse,

    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @field:Schema(description = "리포트 생성 시각", example = "2026-01-09 14:30:25", required = true)
    val createdAt: LocalDateTime,
)

@Schema(title = "SummaryTabResponse", description = "총합 요약 탭")
class SummaryTabResponse(
    @field:Schema(description = "아이 이름", required = false)
    val childName: String?,

    @field:Schema(description = "이야기 제목", required = true)
    val storyTitle: String,

    @field:Schema(description = "이야기 대표 이미지 URL", example = "/files/story-banggui.png", required = false)
    val storyImageUrl: String?,

    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @field:Schema(description = "활동 날짜", example = "2026-01-09 14:30:25", required = true)
    val activityDate: LocalDateTime,

    @field:Schema(description = "활동 소요 시간(분)", example = "20", required = true)
    val durationMinutes: Long,

    @field:Schema(description = "후활동 완료 여부", required = true)
    val postActivityCompleted: PostActivityCompletedResponse,

    @field:Schema(description = "전체 총평", required = true)
    val overall: OverallResponse,

    @field:Schema(description = "참여 요약 항목", required = true)
    val participation: List<ParticipationResponse>,
)

@Schema(title = "PostActivityCompletedResponse", description = "후활동 완료 여부")
class PostActivityCompletedResponse(
    @field:Schema(description = "카드 순서 맞추기 완료 여부", example = "true", required = true)
    val cardOrder: Boolean,

    @field:Schema(description = "이야기 재구성 완료 여부", example = "true", required = true)
    val retelling: Boolean,
)

@Schema(title = "OverallResponse", description = "전체 총평")
class OverallResponse(
    @field:Schema(description = "총평 헤드라인", required = true)
    val headline: String,

    @field:Schema(description = "총평 설명", required = true)
    val description: String,

    @field:Schema(description = "총평 칩", required = true)
    val chips: List<String>,
)

@Schema(title = "ParticipationResponse", description = "참여 요약 항목")
class ParticipationResponse(
    @field:Schema(description = "항목 제목", required = true)
    val title: String,

    @field:Schema(description = "항목 설명", required = true)
    val description: String,
)

@Schema(title = "SpeechTabResponse", description = "어휘·표현·논리 탭")
class SpeechTabResponse(
    @field:Schema(description = "말하기 3영역 분석", required = true)
    val areas: List<SpeechAreaResponse>,
)

@Schema(title = "SpeechAreaResponse", description = "말하기 영역 분석")
class SpeechAreaResponse(
    @field:Schema(description = "영역 이름", example = "어휘", required = true)
    val area: String,

    @field:Schema(description = "영역 요약", required = true)
    val summary: String,

    @field:Schema(description = "핵심 키워드", required = true)
    val keywords: List<String>,

    @field:Schema(description = "영역 특징", required = true)
    val feature: String,

    @field:Schema(description = "근거 발화", required = false)
    val evidenceUtterance: String?,

    @field:Schema(description = "강점", required = true)
    val strength: String,

    @field:Schema(description = "개선점", required = true)
    val improvement: String,
)

@Schema(title = "SceneTabResponse", description = "장면별 발화 탭")
class SceneTabResponse(
    @field:Schema(description = "대화 장면별 발화 카드", required = true)
    val cards: List<SceneCardResponse>,
)

@Schema(title = "SceneCardResponse", description = "장면별 발화 카드")
class SceneCardResponse(
    @field:Schema(description = "대화 장면 순번(1부터, 나레이션 장면 제외)", example = "1", required = true)
    val sceneNumber: Int,

    @field:Schema(description = "장면 고유 식별자", required = true)
    val sceneId: String,

    @field:Schema(description = "장면 제목", required = false)
    val title: String?,

    @field:Schema(description = "장면 이미지 URL", required = false)
    val imageUrl: String?,

    @field:Schema(description = "장면 상황", required = true)
    val situation: String,

    @field:Schema(description = "직전 캐릭터 질문", required = false)
    val characterQuestion: String?,

    @field:Schema(description = "아이 발화", required = true)
    val childUtterance: ChildUtteranceResponse,

    @field:Schema(description = "특징 문장", required = true)
    val featureSentence: String,

    @field:Schema(description = "특징 칩", required = true)
    val featureChips: List<String>,
)

@Schema(title = "ChildUtteranceResponse", description = "아이 발화")
class ChildUtteranceResponse(
    @field:Schema(description = "발화 확정 텍스트", required = true)
    val text: String,

    @field:Schema(description = "발화 음성 파일 URL (음성 미제출 시 null)", required = false)
    val audioUrl: String?,

    @field:Schema(description = "발화 STT 원문", required = false)
    val sttRawText: String?,
)

@Schema(title = "RepresentativeTabResponse", description = "대표 발화 탭")
class RepresentativeTabResponse(
    @field:Schema(description = "대표 발화 텍스트", required = false)
    val text: String?,

    @field:Schema(description = "대표 발화 음성 파일 URL (음성 미제출 시 null)", required = false)
    val audioUrl: String?,

    @field:Schema(description = "해설", required = true)
    val commentary: String,

    @field:Schema(description = "칩", required = true)
    val chips: List<String>,

    @field:Schema(description = "발화 상황", required = true)
    val situation: String,

    @field:Schema(description = "선정 이유", required = true)
    val reason: String,

    @field:Schema(description = "강점", required = true)
    val strengths: String,

    @field:Schema(description = "연습 팁", required = true)
    val practiceTip: String,
)

@Schema(title = "HomeGuideTabResponse", description = "가정 대화 가이드 탭")
class HomeGuideTabResponse(
    @field:Schema(description = "대화 방향", required = true)
    val direction: GuideDirectionResponse,

    @field:Schema(description = "이야기 관련 질문", required = true)
    val storyQuestions: List<GuideQuestionResponse>,

    @field:Schema(description = "일상 연결 질문", required = true)
    val dailyQuestions: List<GuideQuestionResponse>,

    @field:Schema(description = "보호자 팁", required = true)
    val guardianTip: String,
)

@Schema(title = "GuideDirectionResponse", description = "대화 방향")
class GuideDirectionResponse(
    @field:Schema(description = "방향 헤드라인", required = true)
    val headline: String,

    @field:Schema(description = "방향 설명", required = true)
    val description: String,
)

@Schema(title = "GuideQuestionResponse", description = "가정 대화 질문")
class GuideQuestionResponse(
    @field:Schema(description = "질문 라벨", required = true)
    val label: String,

    @field:Schema(description = "질문", required = true)
    val question: String,

    @field:Schema(description = "도움말", required = true)
    val helper: String,
)

fun sessionReportResponse(result: SessionReportResult): SessionReportResponse = SessionReportResponse(
    summaryTab = summaryTab(result.summary, result.overall, result.participation),
    speechTab = SpeechTabResponse(areas = result.speechAnalyses.map { speechArea(it) }),
    sceneTab = SceneTabResponse(cards = result.sceneCards.map { sceneCard(it) }),
    representativeTab = representativeTab(result.representative),
    homeGuideTab = homeGuideTab(result.homeGuide),
    createdAt = result.createdAt,
)

private fun summaryTab(
    summary: SessionReportSummary,
    overall: ReportOverall,
    participation: List<ParticipationItem>,
): SummaryTabResponse = SummaryTabResponse(
    childName = summary.childName,
    storyTitle = summary.storyTitle,
    storyImageUrl = summary.storyImageUrl,
    activityDate = summary.activityDate,
    durationMinutes = summary.durationMinutes,
    postActivityCompleted = PostActivityCompletedResponse(
        cardOrder = summary.cardOrderCompleted,
        retelling = summary.retellingCompleted,
    ),
    overall = OverallResponse(headline = overall.headline, description = overall.description, chips = overall.chips),
    participation = participation.map { ParticipationResponse(title = it.title, description = it.description) },
)

private fun speechArea(analysis: ReportSpeechAnalysis): SpeechAreaResponse = SpeechAreaResponse(
    area = analysis.area,
    summary = analysis.summary,
    keywords = analysis.keywords,
    feature = analysis.feature,
    evidenceUtterance = analysis.evidenceUtterance,
    strength = analysis.strength,
    improvement = analysis.improvement,
)

private fun sceneCard(card: SessionReportSceneCard): SceneCardResponse = SceneCardResponse(
    sceneNumber = card.sceneNumber,
    sceneId = card.sceneId,
    title = card.title,
    imageUrl = card.imageUrl,
    situation = card.situation,
    characterQuestion = card.characterQuestion,
    childUtterance = childUtterance(card.childUtterance),
    featureSentence = card.featureSentence,
    featureChips = card.featureChips,
)

private fun childUtterance(utterance: SessionReportChildUtterance): ChildUtteranceResponse = ChildUtteranceResponse(
    text = utterance.text,
    audioUrl = utterance.audioUrl,
    sttRawText = utterance.sttRawText,
)

private fun representativeTab(representative: SessionReportRepresentative): RepresentativeTabResponse =
    RepresentativeTabResponse(
        text = representative.text,
        audioUrl = representative.audioUrl,
        commentary = representative.commentary,
        chips = representative.chips,
        situation = representative.situation,
        reason = representative.reason,
        strengths = representative.strengths,
        practiceTip = representative.practiceTip,
    )

private fun homeGuideTab(homeGuide: HomeGuide): HomeGuideTabResponse = HomeGuideTabResponse(
    direction = guideDirection(homeGuide.direction),
    storyQuestions = homeGuide.storyQuestions.map { guideQuestion(it) },
    dailyQuestions = homeGuide.dailyQuestions.map { guideQuestion(it) },
    guardianTip = homeGuide.guardianTip,
)

private fun guideDirection(direction: GuideDirection): GuideDirectionResponse =
    GuideDirectionResponse(headline = direction.headline, description = direction.description)

private fun guideQuestion(question: GuideQuestion): GuideQuestionResponse =
    GuideQuestionResponse(label = question.label, question = question.question, helper = question.helper)
