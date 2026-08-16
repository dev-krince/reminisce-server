package com.krince.reminisce.application.port.`in`.report.command

data class GetLatestSessionReportCommand(
    val childId: String,
    val guardianId: String,
)
