package com.krince.reminisce.application.port.`in`.profileinterview.command

class SubmitInterviewUtteranceCommand(
    val guardianId: String,
    val interviewId: String,
    val text: String,
    val sttRawText: String? = null,
)
