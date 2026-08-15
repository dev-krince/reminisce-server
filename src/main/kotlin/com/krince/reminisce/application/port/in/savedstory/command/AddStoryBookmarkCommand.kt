package com.krince.reminisce.application.port.`in`.savedstory.command

class AddStoryBookmarkCommand(
    val childId: String,
    val guardianId: String,
    val storyId: String,
)
