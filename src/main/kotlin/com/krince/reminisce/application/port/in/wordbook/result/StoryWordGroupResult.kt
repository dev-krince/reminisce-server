package com.krince.reminisce.application.port.`in`.wordbook.result

class StoryWordGroupResult(
    val storyId: String,
    val storyTitle: String,
    val words: List<StoryWordResult>,
)

class StoryWordResult(
    val word: String,
    val meaning: String,
    val imageUrl: String?,
    val audioUrl: String?,
)
