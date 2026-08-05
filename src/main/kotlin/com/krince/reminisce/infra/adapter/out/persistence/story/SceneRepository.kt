package com.krince.reminisce.infra.adapter.out.persistence.story

import com.krince.reminisce.infra.adapter.out.persistence.story.entity.SceneOrmEntity
import org.springframework.data.jpa.repository.JpaRepository

interface SceneRepository : JpaRepository<SceneOrmEntity, String> {
    fun findAllByStoryIdOrderBySceneOrderAsc(storyId: String): List<SceneOrmEntity>
}
