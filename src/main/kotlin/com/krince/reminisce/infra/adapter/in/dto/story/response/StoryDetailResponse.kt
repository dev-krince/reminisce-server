package com.krince.reminisce.infra.adapter.`in`.dto.story.response

import com.krince.reminisce.application.port.`in`.story.result.StoryDetailResult
import io.swagger.v3.oas.annotations.media.Schema

@Schema(title = "StoryDetailResponse", description = "이야기 상세 응답")
class StoryDetailResponse(
    @field:Schema(description = "이야기 고유 식별자", example = "s_banggui_daughter_in_law_001", required = true)
    val storyId: String,

    @field:Schema(description = "이야기 제목", example = "방귀 뀌는 며느리", required = true)
    val title: String,

    @field:Schema(description = "이야기 도입", example = "옛날 어느 마을에 방귀를 아주 크게 뀌는 며느리가 살았습니다.", required = true)
    val intro: String,

    @field:Schema(description = "이야기 상황", example = "며느리가 방귀를 참아 힘들어하는 상황", required = false)
    val situation: String?,

    @field:Schema(description = "아이 역할", example = "며느리의 고민을 들어주는 친구", required = false)
    val childRole: String?,

    @field:Schema(description = "이야기 장르", example = "전래동화", required = false)
    val genre: String?,

    @field:Schema(description = "말하기 후 활동 설정", required = false)
    val postActivity: PostActivityResponse?,

    @field:Schema(description = "순서대로 정렬된 장면 목록", required = true)
    val scenes: List<SceneResponse>,

    @field:Schema(description = "이야기 난이도", example = "보통", required = true)
    val difficulty: String,

    @field:Schema(description = "이야기의 주요 주제", example = "[\"관계\", \"감정\"]", required = true)
    val topics: List<String>,
)

fun storyDetailResponse(result: StoryDetailResult): StoryDetailResponse = StoryDetailResponse(
    storyId = result.storyId,
    title = result.title,
    intro = result.intro,
    situation = result.situation,
    childRole = result.childRole,
    genre = result.genre,
    postActivity = result.postActivity?.let { postActivityResponse(result = it) },
    scenes = result.scenes.map { sceneResponse(result = it) },
    difficulty = result.difficulty,
    topics = result.topics,
)
