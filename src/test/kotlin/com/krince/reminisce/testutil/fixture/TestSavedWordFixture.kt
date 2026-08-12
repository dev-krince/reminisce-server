package com.krince.reminisce.testutil.fixture

import com.krince.reminisce.infra.adapter.out.persistence.wordbook.SavedWordRepository
import com.krince.reminisce.infra.adapter.out.persistence.wordbook.entity.SavedWordOrmEntity
import org.springframework.stereotype.Component

@Component
class TestSavedWordFixture(
    private val savedWordRepository: SavedWordRepository,
) {
    fun save(entity: SavedWordOrmEntity): SavedWordOrmEntity = savedWordRepository.save(entity)

    fun findAllByChildId(childId: String): List<SavedWordOrmEntity> =
        savedWordRepository.findAllByChildIdOrderByCreatedDateDesc(childId)

    fun count(): Long = savedWordRepository.count()

    fun deleteAllBatch() {
        savedWordRepository.deleteAllInBatch()
    }
}
