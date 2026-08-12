package com.krince.reminisce.domain.model.story

import com.krince.reminisce.domain.model.story.vo.Difficulty
import com.krince.reminisce.domain.model.story.vo.PostActivityConfig
import com.krince.reminisce.domain.model.story.vo.StoryGenre
import com.krince.reminisce.domain.model.story.vo.StoryId
import com.krince.reminisce.domain.model.story.vo.StoryStatus
import java.time.LocalDateTime

class Story(
    val storyId: StoryId,
    val title: String,
    val summary: String,
    val intro: String,
    val situation: String?,
    val childRole: String?,
    val difficulty: Difficulty,
    val estimatedMinutes: Int?,
    val representativeImageUrl: String?,
    val status: StoryStatus,
    val postActivityConfig: PostActivityConfig?,
    val topics: List<String>,
    val genre: StoryGenre? = null,
    scenes: List<Scene>,
    val createdDate: LocalDateTime? = null,
    val modifiedDate: LocalDateTime? = null,
) {
    val scenes: List<Scene> = scenes.sortedBy { it.sceneOrder }

    init {
        val sceneOrders: List<Int> = this.scenes.map { it.sceneOrder }

        require(sceneOrders.distinct().size == sceneOrders.size) { "장면 순서는 중복될 수 없습니다" }
    }
}
