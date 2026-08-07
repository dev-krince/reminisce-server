package com.krince.reminisce.application.port.`in`.postactivity.command

data class SubmitRetellingCommand(
    val sessionId: String,
    val guardianId: String,
    val text: String,
)
