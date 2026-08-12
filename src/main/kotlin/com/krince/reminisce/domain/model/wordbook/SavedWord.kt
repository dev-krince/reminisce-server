package com.krince.reminisce.domain.model.wordbook

import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.wordbook.vo.SavedWordId
import com.krince.reminisce.shared.util.UuidGenerator
import java.time.LocalDateTime

class SavedWord(
    val savedWordId: SavedWordId,
    val childId: ChildId,
    val word: String,
    val meaning: String?,
    val sourceSceneId: String? = null,
    val createdDate: LocalDateTime? = null,
) {
    companion object {
        fun create(
            childId: ChildId,
            word: String,
            meaning: String?,
            sourceSceneId: String?,
        ): SavedWord = SavedWord(
            savedWordId = SavedWordId(UuidGenerator.generate()),
            childId = childId,
            word = word,
            meaning = meaning,
            sourceSceneId = sourceSceneId,
        )
    }
}
