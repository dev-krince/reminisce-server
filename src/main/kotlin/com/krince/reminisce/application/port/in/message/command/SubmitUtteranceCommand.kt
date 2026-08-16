package com.krince.reminisce.application.port.`in`.message.command

class SubmitUtteranceCommand(
    val sessionId: String,
    val guardianId: String,
    val text: String,
    val sttRawText: String?,
    val audioUrl: String? = null,
)
