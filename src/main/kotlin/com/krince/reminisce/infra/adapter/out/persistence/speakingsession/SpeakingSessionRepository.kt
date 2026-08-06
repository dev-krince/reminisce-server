package com.krince.reminisce.infra.adapter.out.persistence.speakingsession

import com.krince.reminisce.infra.adapter.out.persistence.speakingsession.entity.SpeakingSessionOrmEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface SpeakingSessionRepository : JpaRepository<SpeakingSessionOrmEntity, String> {
    fun findByChildIdAndStoryIdAndStatus(
        childId: String,
        storyId: String,
        status: String,
    ): SpeakingSessionOrmEntity?

    fun findAllByChildIdAndStoryId(childId: String, storyId: String): List<SpeakingSessionOrmEntity>

    fun findBySessionId(sessionId: String): SpeakingSessionOrmEntity?

    fun findAllByChildIdAndStatusOrderByLastActivityAtDesc(
        childId: String,
        status: String,
    ): List<SpeakingSessionOrmEntity>

    @Query("SELECT DISTINCT s.storyId FROM SpeakingSessionOrmEntity s WHERE s.childId = :childId")
    fun findDistinctStoryIdsByChildId(childId: String): List<String>

    @Query("SELECT s.sessionId FROM SpeakingSessionOrmEntity s WHERE s.childId IN :childIds")
    fun findSessionIdsByChildIdIn(childIds: List<String>): List<String>

    fun deleteAllByChildIdIn(childIds: List<String>)
}
