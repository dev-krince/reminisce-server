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

    @field:Schema(description = "이야기의 주요 주제", example = "[\"다름\", \"자기이해\"]", required = true)
    val topics: List<String>,
)

fun storySummaryResponse(result: StorySummaryResult): StorySummaryResponse = StorySummaryResponse(
    storyId = result.storyId,
    title = result.title,
    representativeImageUrl = result.representativeImageUrl,
    estimatedMinutes = result.estimatedMinutes,
    topics = result.topics,
)
