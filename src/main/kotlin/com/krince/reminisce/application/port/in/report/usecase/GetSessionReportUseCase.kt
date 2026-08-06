package com.krince.reminisce.application.port.`in`.report.usecase

import com.krince.reminisce.application.port.`in`.report.command.GetSessionReportCommand
import com.krince.reminisce.application.port.`in`.report.result.SessionReportResult

interface GetSessionReportUseCase {
    fun execute(command: GetSessionReportCommand): SessionReportResult
}
