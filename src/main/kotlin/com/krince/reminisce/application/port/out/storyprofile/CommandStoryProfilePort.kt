package com.krince.reminisce.application.port.out.storyprofile

import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.storyprofile.StoryProfile

interface CommandStoryProfilePort {
    fun save(profile: StoryProfile): StoryProfile

    fun deleteAllByChildIds(childIds: List<ChildId>)
}
