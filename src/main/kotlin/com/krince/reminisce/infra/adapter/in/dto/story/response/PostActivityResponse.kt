package com.krince.reminisce.infra.adapter.`in`.dto.story.response

import com.krince.reminisce.application.port.`in`.story.result.PostActivityConfigResult
import io.swagger.v3.oas.annotations.media.Schema

@Schema(title = "PostActivityResponse", description = "말하기 후 활동 설정 응답")
class PostActivityResponse(
    @field:Schema(description = "순서 배열 카드 목록", required = true)
    val cards: List<PostActivityCardResponse>,

    @field:Schema(description = "이야기 재구성 핵심 단어", example = "[\"며느리\", \"방귀\", \"배나무\"]", required = true)
    val retellingKeywords: List<String>,
)

@Schema(title = "PostActivityCardResponse", description = "말하기 후 활동 카드 응답")
class PostActivityCardResponse(
    @field:Schema(description = "카드 식별자", example = "card_1", required = true)
    val id: String,

    @field:Schema(description = "카드 내용", example = "며느리가 방귀를 참았어요.", required = true)
    val text: String,

    @field:Schema(description = "카드 정답 순서", example = "1", required = true)
    val correctOrder: Int,
)

fun postActivityResponse(result: PostActivityConfigResult): PostActivityResponse = PostActivityResponse(
    cards = result.cards.map { PostActivityCardResponse(id = it.id, text = it.text, correctOrder = it.correctOrder) },
    retellingKeywords = result.retellingKeywords,
)
