package com.krince.reminisce.application.port.`in`.savedstory.command

class RemoveStoryBookmarkCommand(
    val childId: String,
    val guardianId: String,
    val storyId: String,
)
