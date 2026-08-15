package com.krince.reminisce.application.facade.savedstory

import com.krince.reminisce.application.port.access.savedstory.SavedStoryAccessPort
import com.krince.reminisce.application.port.out.savedstory.LoadSavedStoryPort
import com.krince.reminisce.domain.model.child.vo.ChildId
import org.springframework.stereotype.Service

@Service
class SavedStoryAccessFacade(
    private val loadSavedStoryPort: LoadSavedStoryPort,
) : SavedStoryAccessPort {

    override fun findBookmarkedStoryIds(childId: ChildId): Set<String> =
        loadSavedStoryPort.findAllByChildId(childId).map { it.storyId.value }.toSet()
}
