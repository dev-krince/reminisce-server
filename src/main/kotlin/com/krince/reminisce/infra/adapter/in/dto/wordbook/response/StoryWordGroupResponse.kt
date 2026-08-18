package com.krince.reminisce.infra.adapter.`in`.dto.wordbook.response

import com.krince.reminisce.application.port.`in`.wordbook.result.StoryWordGroupResult
import com.krince.reminisce.application.port.`in`.wordbook.result.StoryWordResult
import io.swagger.v3.oas.annotations.media.Schema

@Schema(title = "StoryWordGroupResponse", description = "이야기에서 만난 단어 목록 응답 (이야기별 그룹)")
class StoryWordGroupResponse(
    @field:Schema(description = "이야기 고유 식별자", example = "s_banggui_daughter_in_law_001", required = true)
    val storyId: String,

    @field:Schema(description = "이야기 제목", example = "방귀 뀌는 며느리", required = true)
    val storyTitle: String,

    @field:Schema(description = "이야기의 단어 수", example = "6", required = true)
    val totalWords: Int,

    @field:Schema(description = "단어 목록", required = true)
    val words: List<StoryWordItemResponse>,
)

@Schema(title = "StoryWordItemResponse", description = "이야기에서 만난 단어")
class StoryWordItemResponse(
    @field:Schema(description = "단어", example = "방귀를 참다", required = true)
    val word: String,

    @field:Schema(description = "아이 눈높이 뜻", example = "나오려는 방귀를 뀌지 않고 꾹 견디는 것", required = true)
    val meaning: String,

    @field:Schema(description = "단어 삽화 이미지 URL", example = "/files/banggui-word-01.png", required = false)
    val imageUrl: String?,

    @field:Schema(description = "단어 발음 오디오 URL (카드 선택 시 재생)", example = "/files/tts-narrator-banggui.mp3", required = false)
    val audioUrl: String?,
)

fun storyWordGroupResponse(result: StoryWordGroupResult): StoryWordGroupResponse = StoryWordGroupResponse(
    storyId = result.storyId,
    storyTitle = result.storyTitle,
    totalWords = result.words.size,
    words = result.words.map { storyWordItemResponse(it) },
)

fun storyWordItemResponse(result: StoryWordResult): StoryWordItemResponse = StoryWordItemResponse(
    word = result.word,
    meaning = result.meaning,
    imageUrl = result.imageUrl,
    audioUrl = result.audioUrl,
)
