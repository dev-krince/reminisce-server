package com.krince.reminisce.infra.adapter.out.persistence.savedstory

import com.krince.reminisce.infra.adapter.out.persistence.savedstory.entity.SavedStoryOrmEntity
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Component
class SavedStoryInserter(
    private val repository: SavedStoryRepository,
) {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun insert(ormEntity: SavedStoryOrmEntity): SavedStoryOrmEntity = repository.saveAndFlush(ormEntity)
}
