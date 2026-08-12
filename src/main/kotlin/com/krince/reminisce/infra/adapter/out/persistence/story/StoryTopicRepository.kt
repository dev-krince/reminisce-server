package com.krince.reminisce.infra.adapter.out.persistence.story

import com.krince.reminisce.infra.adapter.out.persistence.story.entity.StoryTopicOrmEntity
import org.springframework.data.jpa.repository.JpaRepository

interface StoryTopicRepository : JpaRepository<StoryTopicOrmEntity, String> {
    fun findAllByStoryId(storyId: String): List<StoryTopicOrmEntity>

    fun findAllByStoryIdIn(storyIds: Collection<String>): List<StoryTopicOrmEntity>
}
