package com.krince.reminisce.application.port.`in`.mission.command

data class SubmitMissionAnswerCommand(
    val sessionId: String,
    val guardianId: String,
    val sceneId: String,
    val submittedOrder: List<String>? = null,
    val text: String? = null,
)
