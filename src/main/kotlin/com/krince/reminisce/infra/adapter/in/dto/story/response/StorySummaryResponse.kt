package com.krince.reminisce.infra.adapter.`in`.dto.story.response

import com.krince.reminisce.application.port.`in`.story.result.StorySummaryResult
import io.swagger.v3.oas.annotations.media.Schema

@Schema(title = "StorySummaryResponse", description = "이야기 목록 요약 응답")
class StorySummaryResponse(
    @field:Schema(description = "이야기 고유 식별자", example = "s_banggui_daughter_in_law_001", required = true)
    val storyId: String,

    @field:Schema(description = "이야기 제목", example = "방귀 뀌는 며느리", required = true)
    val title: String,

    @field:Schema(description = "대표 이미지 URL", example = "/files/story-image.png", required = false)
    val representativeImageUrl: String?,

    @field:Schema(description = "예상 활동 시간(분)", example = "20", required = false)
    val estimatedMinutes: Int?,

    @field:Schema(description = "이야기의 주요 주제", example = "[\"관계\", \"감정\"]", required = true)
    val topics: List<String>,

    @field:Schema(description = "이야기 장르", example = "전래동화", required = false)
    val genre: String?,

    @field:Schema(description = "이야기 난이도", example = "보통", required = true)
    val difficulty: String,

    @field:Schema(description = "요청 아이의 찜 여부(childId 미지정 시 false)", example = "false", required = true)
    val isBookmarked: Boolean,
)

fun storySummaryResponse(result: StorySummaryResult): StorySummaryResponse = StorySummaryResponse(
    storyId = result.storyId,
    title = result.title,
    representativeImageUrl = result.representativeImageUrl,
    estimatedMinutes = result.estimatedMinutes,
    topics = result.topics,
    genre = result.genre,
    difficulty = result.difficulty,
    isBookmarked = result.isBookmarked,
)
