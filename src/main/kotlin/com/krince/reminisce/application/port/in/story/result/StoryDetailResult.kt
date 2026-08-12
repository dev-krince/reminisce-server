package com.krince.reminisce.application.port.`in`.story.result

import com.krince.reminisce.domain.model.story.Story

class StoryDetailResult(
    val storyId: String,
    val title: String,
    val intro: String,
    val situation: String?,
    val childRole: String?,
    val genre: String?,
    val postActivity: PostActivityConfigResult?,
    val scenes: List<SceneResult>,
) {
    companion object {
        fun from(story: Story): StoryDetailResult = StoryDetailResult(
            storyId = story.storyId.value,
            title = story.title,
            intro = story.intro,
            situation = story.situation,
            childRole = story.childRole,
            genre = story.genre?.label,
            postActivity = story.postActivityConfig?.let { PostActivityConfigResult.from(it) },
            scenes = story.scenes.map { SceneResult.from(it) },
        )
    }
}
