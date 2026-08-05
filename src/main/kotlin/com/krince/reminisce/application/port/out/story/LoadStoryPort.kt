package com.krince.reminisce.application.port.out.story

import com.krince.reminisce.domain.model.story.Story
import com.krince.reminisce.domain.model.story.vo.StoryId

interface LoadStoryPort {
    fun findAllPublished(): List<Story>

    fun findAllPublishedByTopic(topic: String): List<Story>

    fun findByIdWithScenesPublished(storyId: StoryId): Story?
}
