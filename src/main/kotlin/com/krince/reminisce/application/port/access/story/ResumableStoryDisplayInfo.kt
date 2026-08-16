package com.krince.reminisce.application.port.access.story

class ResumableStoryDisplayInfo(
    val title: String,
    val representativeImageUrl: String?,
    val difficulty: String,
    val topics: List<String>,
    val currentChapter: Int,
    val totalChapters: Int,
)
