package com.krince.reminisce.application.port.`in`.story.result

import com.krince.reminisce.domain.model.story.vo.PostActivityConfig

class PostActivityConfigResult(
    val cards: List<CardResult>,
    val retellingKeywords: List<String>,
) {
    class CardResult(
        val id: String,
        val text: String,
        val correctOrder: Int,
        val imageUrl: String?,
    )

    companion object {
        fun from(postActivityConfig: PostActivityConfig): PostActivityConfigResult = PostActivityConfigResult(
            cards = postActivityConfig.cards.map {
                CardResult(id = it.id, text = it.text, correctOrder = it.correctOrder, imageUrl = it.imageUrl)
            },
            retellingKeywords = postActivityConfig.retellingKeywords,
        )
    }
}
