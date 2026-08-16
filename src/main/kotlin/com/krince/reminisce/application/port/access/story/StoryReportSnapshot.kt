package com.krince.reminisce.application.port.access.story

class StoryReportSnapshot(
    val title: String,
    val scenes: List<StoryReportScene>,
)

class StoryReportScene(
    val sceneId: String,
    val description: String,
    val goal: String?,
)
