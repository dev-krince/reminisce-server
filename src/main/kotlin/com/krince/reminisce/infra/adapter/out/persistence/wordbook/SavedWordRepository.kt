package com.krince.reminisce.infra.adapter.out.persistence.wordbook

import com.krince.reminisce.infra.adapter.out.persistence.wordbook.entity.SavedWordOrmEntity
import org.springframework.data.jpa.repository.JpaRepository

interface SavedWordRepository : JpaRepository<SavedWordOrmEntity, String> {
    fun findAllByChildIdOrderByCreatedDateDesc(childId: String): List<SavedWordOrmEntity>

    fun deleteAllByChildIdIn(childIds: List<String>)
}
