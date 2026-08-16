package com.krince.reminisce.infra.adapter.out.persistence.storyprofile

import com.krince.reminisce.infra.adapter.out.persistence.storyprofile.entity.StoryProfileOrmEntity
import org.springframework.data.jpa.repository.JpaRepository

interface StoryProfileRepository : JpaRepository<StoryProfileOrmEntity, String> {
    fun findByChildId(childId: String): StoryProfileOrmEntity?

    fun deleteAllByChildIdIn(childIds: List<String>)
}
