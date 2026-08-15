package com.krince.reminisce.application.port.`in`.savedstory.result

import com.krince.reminisce.domain.model.savedstory.SavedStory
import java.time.LocalDateTime

class BookmarkedStoryResult(
    val savedStoryId: String,
    val storyId: String,
    val createdAt: LocalDateTime?,
) {
    companion object {
        fun from(savedStory: SavedStory): BookmarkedStoryResult = BookmarkedStoryResult(
            savedStoryId = savedStory.savedStoryId.value,
            storyId = savedStory.storyId.value,
            createdAt = savedStory.createdDate,
        )
    }
}
