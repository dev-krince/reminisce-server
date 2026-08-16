package com.krince.reminisce.infra.adapter.out.persistence.message

import com.krince.reminisce.application.port.out.message.CommandMessagePort
import com.krince.reminisce.application.port.out.message.LoadMessagePort
import com.krince.reminisce.domain.model.message.Message
import com.krince.reminisce.domain.model.speakingsession.vo.SpeakingSessionId
import com.krince.reminisce.infra.adapter.out.persistence.message.entity.MessageOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.message.mapper.MessageMapper
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component

@Component
class MessageOrmAdapter(
    private val repository: MessageRepository,
) : CommandMessagePort, LoadMessagePort {

    override fun save(message: Message): Message {
        val ormEntity: MessageOrmEntity = MessageMapper.toEntity(message)
        val savedEntity: MessageOrmEntity = repository.saveAndFlush(ormEntity)

        return MessageMapper.toDomain(savedEntity)
    }

    override fun countBySession(sessionId: SpeakingSessionId): Long =
        repository.countBySessionId(sessionId.value)

    override fun findAllBySession(sessionId: SpeakingSessionId): List<Message> =
        repository.findAllBySessionId(sessionId.value)
            .sortedBy { it.turnOrder }
            .map { MessageMapper.toDomain(it) }

    override fun findRecentMessagesBySession(sessionId: SpeakingSessionId, limit: Int): List<Message> {
        if (limit <= 0) {
            return emptyList()
        }
        val entities: List<MessageOrmEntity> =
            repository.findBySessionIdOrderByTurnOrderDesc(sessionId.value, PageRequest.of(0, limit))

        return entities.asReversed().map { MessageMapper.toDomain(it) }
    }

    override fun findMessageIdsBySessionIds(sessionIds: List<String>): List<String> {
        if (sessionIds.isEmpty()) {
            return emptyList()
        }

        return repository.findMessageIdsBySessionIdIn(sessionIds)
    }

    override fun findAudioUrlsBySessionIds(sessionIds: List<String>): List<String> {
        if (sessionIds.isEmpty()) {
            return emptyList()
        }

        return repository.findAudioUrlsBySessionIdIn(sessionIds)
    }

    override fun deleteAllBySessionIds(sessionIds: List<String>) {
        if (sessionIds.isEmpty()) {
            return
        }

        repository.deleteAllBySessionIdIn(sessionIds)
    }
}
