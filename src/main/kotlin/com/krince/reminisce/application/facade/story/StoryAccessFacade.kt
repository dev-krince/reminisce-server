package com.krince.reminisce.application.facade.story

import com.krince.reminisce.application.port.access.story.StoryAccessPort
import com.krince.reminisce.application.port.out.story.LoadStoryPort
import com.krince.reminisce.domain.model.story.vo.StoryId
import org.springframework.stereotype.Service

@Service
class StoryAccessFacade(
    private val loadStoryPort: LoadStoryPort,
) : StoryAccessPort {

    override fun existsPublished(storyId: StoryId): Boolean =
        loadStoryPort.findByIdWithScenesPublished(storyId) != null
}
