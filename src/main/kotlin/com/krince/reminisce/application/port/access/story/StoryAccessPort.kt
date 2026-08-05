package com.krince.reminisce.application.port.access.story

import com.krince.reminisce.domain.model.story.vo.StoryId

interface StoryAccessPort {
    fun existsPublished(storyId: StoryId): Boolean
}
