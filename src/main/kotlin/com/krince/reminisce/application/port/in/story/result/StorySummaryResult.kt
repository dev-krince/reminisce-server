package com.krince.reminisce.application.port.`in`.story.result

import com.krince.reminisce.domain.model.story.Story

class StorySummaryResult(
    val storyId: String,
    val title: String,
    val representativeImageUrl: String?,
    val estimatedMinutes: Int?,
    val topics: List<String>,
    val genre: String?,
    val difficulty: String,
) {
    companion object {
        fun from(story: Story): StorySummaryResult = StorySummaryResult(
            storyId = story.storyId.value,
            title = story.title,
            representativeImageUrl = story.representativeImageUrl,
            estimatedMinutes = story.estimatedMinutes,
            topics = story.topics,
            genre = story.genre?.label,
            difficulty = story.difficulty.value,
        )
    }
}
