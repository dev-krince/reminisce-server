package com.krince.reminisce.testutil.fixture

import com.krince.reminisce.infra.adapter.out.persistence.speakingsession.SpeakingSessionRepository
import com.krince.reminisce.infra.adapter.out.persistence.speakingsession.entity.SpeakingSessionOrmEntity
import org.springframework.stereotype.Component

@Component
class TestSpeakingSessionFixture(
    private val speakingSessionRepository: SpeakingSessionRepository,
) {
    fun save(entity: SpeakingSessionOrmEntity): SpeakingSessionOrmEntity = speakingSessionRepository.save(entity)

    fun findAllByChildIdAndStoryId(childId: String, storyId: String): List<SpeakingSessionOrmEntity> =
        speakingSessionRepository.findAllByChildIdAndStoryId(childId, storyId)

    fun count(): Long = speakingSessionRepository.count()

    fun deleteAllBatch() {
        speakingSessionRepository.deleteAllInBatch()
    }
}
