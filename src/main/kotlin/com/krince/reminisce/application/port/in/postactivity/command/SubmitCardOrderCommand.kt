package com.krince.reminisce.application.port.`in`.postactivity.command

data class SubmitCardOrderCommand(
    val sessionId: String,
    val guardianId: String,
    val order: List<String>,
)
