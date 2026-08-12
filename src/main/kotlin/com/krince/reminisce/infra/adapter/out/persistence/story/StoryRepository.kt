package com.krince.reminisce.infra.adapter.out.persistence.story

import com.krince.reminisce.infra.adapter.out.persistence.story.entity.StoryOrmEntity
import org.springframework.data.jpa.repository.JpaRepository

interface StoryRepository : JpaRepository<StoryOrmEntity, String> {
    fun findAllByStatus(status: String): List<StoryOrmEntity>

    fun findByStoryIdAndStatus(storyId: String, status: String): StoryOrmEntity?
}
