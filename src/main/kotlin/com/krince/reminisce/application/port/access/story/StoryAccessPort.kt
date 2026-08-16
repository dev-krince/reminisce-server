package com.krince.reminisce.application.port.access.story

import com.krince.reminisce.domain.model.story.Scene
import com.krince.reminisce.domain.model.story.vo.PostActivityConfig
import com.krince.reminisce.domain.model.story.vo.StoryId

interface StoryAccessPort {
    fun existsPublished(storyId: StoryId): Boolean

    fun findIntro(storyId: StoryId): String?

    fun findFirstSceneId(storyId: StoryId): String?

    fun findScene(storyId: StoryId, sceneId: String): Scene?

    fun findNextScene(storyId: StoryId, currentSceneId: String): Scene?

    fun findPreviousChapterFirstScene(storyId: StoryId, currentSceneId: String): Scene?

    fun findPrecedingCharacterLine(storyId: StoryId, currentSceneId: String): Scene?

    fun findPostActivityConfig(storyId: StoryId): PostActivityConfig?

    fun findResumableDisplayInfo(storyId: StoryId, currentSceneId: String?): ResumableStoryDisplayInfo?

    fun findReportSnapshot(storyId: StoryId): StoryReportSnapshot?
}
