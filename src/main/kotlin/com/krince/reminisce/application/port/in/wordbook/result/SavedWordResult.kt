package com.krince.reminisce.application.port.`in`.wordbook.result

import com.krince.reminisce.domain.model.wordbook.SavedWord
import java.time.LocalDateTime

class SavedWordResult(
    val savedWordId: String,
    val word: String,
    val meaning: String?,
    val sourceSceneId: String?,
    val createdAt: LocalDateTime?,
) {
    companion object {
        fun from(savedWord: SavedWord): SavedWordResult = SavedWordResult(
            savedWordId = savedWord.savedWordId.value,
            word = savedWord.word,
            meaning = savedWord.meaning,
            sourceSceneId = savedWord.sourceSceneId,
            createdAt = savedWord.createdDate,
        )
    }
}
