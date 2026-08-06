package com.krince.reminisce.testutil.fixture

import com.krince.reminisce.infra.adapter.out.persistence.message.MessageRepository
import com.krince.reminisce.infra.adapter.out.persistence.message.entity.MessageOrmEntity
import org.springframework.stereotype.Component

@Component
class TestMessageFixture(
    private val messageRepository: MessageRepository,
) {
    fun save(entity: MessageOrmEntity): MessageOrmEntity = messageRepository.save(entity)

    fun count(): Long = messageRepository.count()

    fun countBySessionId(sessionId: String): Long = messageRepository.countBySessionId(sessionId)

    fun findAllBySessionId(sessionId: String): List<MessageOrmEntity> =
        messageRepository.findAllBySessionId(sessionId)

    fun deleteAllBatch() {
        messageRepository.deleteAllInBatch()
    }
}
