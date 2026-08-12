package com.krince.reminisce.application.port.out.wordbook

import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.wordbook.SavedWord

interface CommandSavedWordPort {
    fun save(savedWord: SavedWord): SavedWord

    fun deleteAllByChildIds(childIds: List<ChildId>)
}
