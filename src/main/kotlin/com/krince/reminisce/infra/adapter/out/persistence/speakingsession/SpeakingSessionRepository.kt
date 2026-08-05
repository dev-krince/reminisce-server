package com.krince.reminisce.infra.adapter.out.persistence.speakingsession

import com.krince.reminisce.infra.adapter.out.persistence.speakingsession.entity.SpeakingSessionOrmEntity
import org.springframework.data.jpa.repository.JpaRepository

interface SpeakingSessionRepository : JpaRepository<SpeakingSessionOrmEntity, String> {
    fun findByChildIdAndStoryIdAndStatus(
        childId: String,
        storyId: String,
        status: String,
    ): SpeakingSessionOrmEntity?

    fun findAllByChildIdAndStoryId(childId: String, storyId: String): List<SpeakingSessionOrmEntity>
}
