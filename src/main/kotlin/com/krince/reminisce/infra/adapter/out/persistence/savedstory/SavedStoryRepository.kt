package com.krince.reminisce.infra.adapter.out.persistence.savedstory

import com.krince.reminisce.infra.adapter.out.persistence.savedstory.entity.SavedStoryOrmEntity
import org.springframework.data.jpa.repository.JpaRepository

interface SavedStoryRepository : JpaRepository<SavedStoryOrmEntity, String> {
    fun findAllByChildIdOrderByCreatedDateDesc(childId: String): List<SavedStoryOrmEntity>

    fun findByChildIdAndStoryId(childId: String, storyId: String): SavedStoryOrmEntity?

    fun deleteByChildIdAndStoryId(childId: String, storyId: String)

    fun deleteAllByChildIdIn(childIds: List<String>)
}
