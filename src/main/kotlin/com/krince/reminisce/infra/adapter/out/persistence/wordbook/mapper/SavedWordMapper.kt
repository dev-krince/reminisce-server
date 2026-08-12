package com.krince.reminisce.infra.adapter.out.persistence.wordbook.mapper

import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.wordbook.SavedWord
import com.krince.reminisce.domain.model.wordbook.vo.SavedWordId
import com.krince.reminisce.infra.adapter.out.persistence.wordbook.entity.SavedWordOrmEntity

object SavedWordMapper {
    fun toDomain(ormEntity: SavedWordOrmEntity): SavedWord = SavedWord(
        savedWordId = SavedWordId(ormEntity.savedWordId),
        childId = ChildId(ormEntity.childId),
        word = ormEntity.word,
        meaning = ormEntity.meaning,
        sourceSceneId = ormEntity.sourceSceneId,
        createdDate = ormEntity.createdDate,
    )

    fun toEntity(domain: SavedWord): SavedWordOrmEntity = SavedWordOrmEntity(
        savedWordId = domain.savedWordId.value,
        childId = domain.childId.value,
        word = domain.word,
        meaning = domain.meaning,
        sourceSceneId = domain.sourceSceneId,
    ).apply {
        createdDate = domain.createdDate
    }
}
