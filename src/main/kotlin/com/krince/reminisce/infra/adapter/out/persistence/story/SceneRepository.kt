package com.krince.reminisce.infra.adapter.out.persistence.story

import com.krince.reminisce.infra.adapter.out.persistence.story.entity.SceneOrmEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface SceneRepository : JpaRepository<SceneOrmEntity, String> {
    fun findAllByStoryIdOrderBySceneOrderAsc(storyId: String): List<SceneOrmEntity>

    @Modifying
    @Query(
        """
        update SceneOrmEntity s
        set s.preferredTurns = :preferredTurns, s.maxTurns = :maxTurns
        where s.sceneId = :sceneId
        """,
    )
    fun updateTurns(
        @Param("sceneId") sceneId: String,
        @Param("preferredTurns") preferredTurns: Short?,
        @Param("maxTurns") maxTurns: Short?,
    ): Int
}
