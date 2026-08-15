package com.krince.reminisce.application.port.access.savedstory

import com.krince.reminisce.domain.model.child.vo.ChildId

interface SavedStoryAccessPort {
    fun findBookmarkedStoryIds(childId: ChildId): Set<String>
}
