package com.krince.reminisce.domain.model.savedstory

import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.savedstory.vo.SavedStoryId
import com.krince.reminisce.domain.model.story.vo.StoryId
import com.krince.reminisce.shared.util.UuidGenerator
import java.time.LocalDateTime

class SavedStory(
    val savedStoryId: SavedStoryId,
    val childId: ChildId,
    val storyId: StoryId,
    val createdDate: LocalDateTime? = null,
) {
    companion object {
        fun create(
            childId: ChildId,
            storyId: StoryId,
        ): SavedStory = SavedStory(
            savedStoryId = SavedStoryId(UuidGenerator.generate()),
            childId = childId,
            storyId = storyId,
        )
    }
}
