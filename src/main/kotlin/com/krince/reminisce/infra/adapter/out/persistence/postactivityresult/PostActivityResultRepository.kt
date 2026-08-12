package com.krince.reminisce.infra.adapter.out.persistence.postactivityresult

import com.krince.reminisce.infra.adapter.out.persistence.postactivityresult.entity.PostActivityResultOrmEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface PostActivityResultRepository : JpaRepository<PostActivityResultOrmEntity, String> {
    fun findBySessionId(sessionId: String): PostActivityResultOrmEntity?

    fun deleteAllBySessionIdIn(sessionIds: List<String>)

    @Query(
        """
        select entity.retellingAudioUrl
        from PostActivityResultOrmEntity entity
        where entity.sessionId in :sessionIds
          and entity.retellingAudioUrl is not null
          and trim(entity.retellingAudioUrl) <> ''
        """,
    )
    fun findRetellingAudioUrlsBySessionIdIn(@Param("sessionIds") sessionIds: List<String>): List<String>
}
