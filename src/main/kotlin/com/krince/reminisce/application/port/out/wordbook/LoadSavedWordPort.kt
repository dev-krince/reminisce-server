package com.krince.reminisce.application.port.out.wordbook

import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.wordbook.SavedWord

interface LoadSavedWordPort {
    fun findAllByChildId(childId: ChildId): List<SavedWord>
}
