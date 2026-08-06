package com.krince.reminisce.infra.adapter.out.persistence.message

import com.krince.reminisce.infra.adapter.out.persistence.message.entity.MessageOrmEntity
import org.springframework.data.jpa.repository.JpaRepository

interface MessageRepository : JpaRepository<MessageOrmEntity, String> {
    fun countBySessionId(sessionId: String): Long

    fun findAllBySessionId(sessionId: String): List<MessageOrmEntity>

    fun findAllBySessionIdAndSpeakerType(sessionId: String, speakerType: String): List<MessageOrmEntity>
}
