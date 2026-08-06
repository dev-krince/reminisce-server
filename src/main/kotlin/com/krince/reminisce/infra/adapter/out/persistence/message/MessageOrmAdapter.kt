package com.krince.reminisce.infra.adapter.out.persistence.message

import com.krince.reminisce.application.port.out.message.CommandMessagePort
import com.krince.reminisce.application.port.out.message.LoadMessagePort
import com.krince.reminisce.domain.model.message.Message
import com.krince.reminisce.domain.model.speakingsession.vo.SpeakingSessionId
import com.krince.reminisce.infra.adapter.out.persistence.message.entity.MessageOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.message.mapper.MessageMapper
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
}
