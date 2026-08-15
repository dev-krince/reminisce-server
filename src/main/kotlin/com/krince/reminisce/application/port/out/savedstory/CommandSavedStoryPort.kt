package com.krince.reminisce.application.port.out.savedstory

import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.savedstory.SavedStory
import com.krince.reminisce.domain.model.story.vo.StoryId

interface CommandSavedStoryPort {
    fun saveIfAbsent(savedStory: SavedStory): SavedStory

    fun deleteByChildIdAndStoryId(childId: ChildId, storyId: StoryId)

    fun deleteAllByChildIds(childIds: List<ChildId>)
}
