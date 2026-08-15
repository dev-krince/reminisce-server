package com.krince.reminisce.testutil.fixture

import com.krince.reminisce.infra.adapter.out.persistence.savedstory.SavedStoryRepository
import com.krince.reminisce.infra.adapter.out.persistence.savedstory.entity.SavedStoryOrmEntity
import org.springframework.stereotype.Component

@Component
class TestSavedStoryFixture(
    private val savedStoryRepository: SavedStoryRepository,
) {
    fun save(entity: SavedStoryOrmEntity): SavedStoryOrmEntity = savedStoryRepository.save(entity)

    fun findAllByChildId(childId: String): List<SavedStoryOrmEntity> =
        savedStoryRepository.findAllByChildIdOrderByCreatedDateDesc(childId)

    fun count(): Long = savedStoryRepository.count()

    fun deleteAllBatch() {
        savedStoryRepository.deleteAllInBatch()
    }
}
