package com.krince.reminisce.application.port.`in`.speakingsession.command

class StartSpeakingSessionCommand(
    val guardianId: String,
    val childId: String,
    val storyId: String,
)
