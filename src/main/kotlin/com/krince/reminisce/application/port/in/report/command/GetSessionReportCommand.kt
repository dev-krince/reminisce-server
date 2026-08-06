package com.krince.reminisce.application.port.`in`.report.command

data class GetSessionReportCommand(
    val sessionId: String,
    val guardianId: String,
)
