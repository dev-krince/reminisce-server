package com.krince.reminisce.infra.adapter.`in`.dto.storyprofile.response

import com.fasterxml.jackson.annotation.JsonFormat
import com.krince.reminisce.application.port.`in`.storyprofile.result.StoryProfileResult
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(title = "InterestTopicResponse", description = "관심 주제 응답")
class InterestTopicResponse(
    @field:Schema(description = "주제 카테고리", example = "관계", required = true)
    val category: String,

    @field:Schema(description = "관심 태그 목록", example = "[\"친구\", \"동물\"]", required = true)
    val tags: List<String>,
)

@Schema(title = "ProfileFindingResponse", description = "프로필 항목(잘하는 것·연습할 것) 응답")
class ProfileFindingResponse(
    @field:Schema(description = "항목 제목", example = "생각을 표현해요", required = true)
    val title: String,

    @field:Schema(description = "항목 설명", example = "자신의 생각과 감정을 말할 수 있어요.", required = true)
    val description: String,
)

@Schema(title = "SpeechAreaAnalysisResponse", description = "말하기 분석 영역 응답")
class SpeechAreaAnalysisResponse(
    @field:Schema(description = "분석 영역", example = "어휘", required = true)
    val area: String,

    @field:Schema(description = "영역 요약 한 문장", example = "상황에 맞는 단어를 자연스럽게 사용했어요.", required = true)
    val summary: String,

    @field:Schema(description = "관련 키워드", example = "[\"부끄럽다\", \"특별하다\"]", required = true)
    val keywords: List<String>,

    @field:Schema(description = "이번 활동에서 나타난 특징", required = true)
    val feature: String,

    @field:Schema(description = "근거가 된 실제 발화", required = false)
    val evidenceUtterance: String?,

    @field:Schema(description = "잘한 점", required = true)
    val strength: String,

    @field:Schema(description = "다음에 연습하면 좋은 점", required = true)
    val improvement: String,
)

@Schema(title = "StoryProfileResponse", description = "아이 이야기 프로필 응답")
class StoryProfileResponse(
    @field:Schema(description = "아이 고유 식별자", required = true)
    val childId: String,

    @field:Schema(description = "근거가 된 프로필 인터뷰 식별자", required = true)
    val interviewId: String,

    @field:Schema(description = "관심 주제 목록", required = true)
    val interestTopics: List<InterestTopicResponse>,

    @field:Schema(description = "잘하는 이야기 방식 목록", required = true)
    val strengths: List<ProfileFindingResponse>,

    @field:Schema(description = "조금 더 연습하면 좋은 점 목록", required = true)
    val practicePoints: List<ProfileFindingResponse>,

    @field:Schema(description = "말하기 분석 3영역 (어휘·표현·논리)", required = true)
    val speechAnalyses: List<SpeechAreaAnalysisResponse>,

    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @field:Schema(description = "프로필 생성 시각", example = "2026-08-17 12:00:00", required = true)
    val createdAt: LocalDateTime,
)

fun storyProfileResponse(result: StoryProfileResult): StoryProfileResponse = StoryProfileResponse(
    childId = result.childId,
    interviewId = result.interviewId,
    interestTopics = result.interestTopics.map { InterestTopicResponse(it.category, it.tags) },
    strengths = result.strengths.map { ProfileFindingResponse(it.title, it.description) },
    practicePoints = result.practicePoints.map { ProfileFindingResponse(it.title, it.description) },
    speechAnalyses = result.speechAnalyses.map {
        SpeechAreaAnalysisResponse(
            area = it.area,
            summary = it.summary,
            keywords = it.keywords,
            feature = it.feature,
            evidenceUtterance = it.evidenceUtterance,
            strength = it.strength,
            improvement = it.improvement,
        )
    },
    createdAt = result.createdAt,
)
