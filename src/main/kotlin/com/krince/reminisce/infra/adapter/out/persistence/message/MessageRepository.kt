package com.krince.reminisce.infra.adapter.out.persistence.message

import com.krince.reminisce.infra.adapter.out.persistence.message.entity.MessageOrmEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface MessageRepository : JpaRepository<MessageOrmEntity, String> {
    fun countBySessionId(sessionId: String): Long

    fun findAllBySessionId(sessionId: String): List<MessageOrmEntity>

    fun findBySessionIdOrderByTurnOrderDesc(sessionId: String, pageable: Pageable): List<MessageOrmEntity>

    fun findAllBySessionIdAndSpeakerType(sessionId: String, speakerType: String): List<MessageOrmEntity>

    @Query("SELECT m.id FROM MessageOrmEntity m WHERE m.sessionId IN :sessionIds")
    fun findMessageIdsBySessionIdIn(sessionIds: List<String>): List<String>

    fun deleteAllBySessionIdIn(sessionIds: List<String>)
}
