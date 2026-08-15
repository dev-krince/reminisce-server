package com.krince.reminisce.application.port.out.savedstory

import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.savedstory.SavedStory
import com.krince.reminisce.domain.model.story.vo.StoryId

interface LoadSavedStoryPort {
    fun findAllByChildId(childId: ChildId): List<SavedStory>

    fun findByChildIdAndStoryId(childId: ChildId, storyId: StoryId): SavedStory?
}
