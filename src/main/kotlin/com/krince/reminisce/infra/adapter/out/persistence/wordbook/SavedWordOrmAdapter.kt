package com.krince.reminisce.infra.adapter.out.persistence.wordbook

import com.krince.reminisce.application.port.out.wordbook.CommandSavedWordPort
import com.krince.reminisce.application.port.out.wordbook.LoadSavedWordPort
import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.wordbook.SavedWord
import com.krince.reminisce.infra.adapter.out.persistence.wordbook.entity.SavedWordOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.wordbook.mapper.SavedWordMapper
import org.springframework.stereotype.Component

@Component
class SavedWordOrmAdapter(
    private val repository: SavedWordRepository,
) : LoadSavedWordPort, CommandSavedWordPort {

    override fun save(savedWord: SavedWord): SavedWord {
        val ormEntity: SavedWordOrmEntity = SavedWordMapper.toEntity(savedWord)
        val savedEntity: SavedWordOrmEntity = repository.saveAndFlush(ormEntity)

        return SavedWordMapper.toDomain(savedEntity)
    }

    override fun findAllByChildId(childId: ChildId): List<SavedWord> =
        repository.findAllByChildIdOrderByCreatedDateDesc(childId.value).map { SavedWordMapper.toDomain(it) }

    override fun deleteAllByChildIds(childIds: List<ChildId>) {
        if (childIds.isEmpty()) {
            return
        }

        repository.deleteAllByChildIdIn(childIds.map { it.value })
    }
}
