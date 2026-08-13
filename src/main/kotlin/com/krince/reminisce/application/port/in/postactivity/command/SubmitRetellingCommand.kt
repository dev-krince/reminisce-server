package com.krince.reminisce.application.port.`in`.postactivity.command

data class SubmitRetellingCommand(
    val sessionId: String,
    val guardianId: String,
    val text: String,
    val sceneSegments: List<String>? = null,
    val retellingAudioUrl: String? = null,
)
