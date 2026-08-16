package com.krince.reminisce.application.port.access.story

import com.krince.reminisce.domain.model.story.vo.SceneType

class StoryReportSnapshot(
    val title: String,
    val scenes: List<StoryReportScene>,
)

class StoryReportScene(
    val sceneId: String,
    val sceneOrder: Int,
    val sceneType: SceneType,
    val description: String,
    val goal: String?,
    val sceneTitle: String?,
    val imageUrl: String?,
    val characterDisplayName: String?,
)
