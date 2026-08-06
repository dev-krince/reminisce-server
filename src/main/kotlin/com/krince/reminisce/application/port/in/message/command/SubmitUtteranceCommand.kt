package com.krince.reminisce.application.port.`in`.message.command

class SubmitUtteranceCommand(
    val sessionId: String,
    val guardianId: String,
    val audio: String,
)
