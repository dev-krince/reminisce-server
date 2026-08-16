package com.krince.reminisce.application.port.`in`.mission.result

data class MissionAnswerResult(
    val completed: Boolean,
    val attemptCount: Int,
    val hints: List<String>,
)
