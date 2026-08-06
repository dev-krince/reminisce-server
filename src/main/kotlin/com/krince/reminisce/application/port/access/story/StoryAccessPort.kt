package com.krince.reminisce.application.port.access.story

import com.krince.reminisce.domain.model.story.Scene
import com.krince.reminisce.domain.model.story.vo.StoryId

interface StoryAccessPort {
    fun existsPublished(storyId: StoryId): Boolean

    fun findIntro(storyId: StoryId): String?

    fun findFirstSceneId(storyId: StoryId): String?

    fun findScene(storyId: StoryId, sceneId: String): Scene?
}
