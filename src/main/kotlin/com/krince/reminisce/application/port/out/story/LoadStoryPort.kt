package com.krince.reminisce.application.port.out.story

import com.krince.reminisce.domain.model.story.Story
import com.krince.reminisce.domain.model.story.vo.StoryGenre
import com.krince.reminisce.domain.model.story.vo.StoryId
import com.krince.reminisce.domain.model.story.vo.StorySort

interface LoadStoryPort {
    fun findAllPublished(): List<Story>

    fun findAllPublishedByTopic(topic: String): List<Story>

    fun findPublished(genre: StoryGenre?, topic: String?, titleKeyword: String?, sort: StorySort): List<Story>

    fun findByIdWithScenesPublished(storyId: StoryId): Story?
}
