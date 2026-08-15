package com.krince.reminisce.domain.model.story.vo

data class PostActivityConfig(
    val cards: List<Card>,
    val retellingKeywords: List<String>,
) {
    data class Card(
        val id: String,
        val text: String,
        val correctOrder: Int,
        val imageUrl: String? = null,
    )
}
