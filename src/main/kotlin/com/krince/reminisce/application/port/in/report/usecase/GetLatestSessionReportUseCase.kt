package com.krince.reminisce.application.port.`in`.report.usecase

import com.krince.reminisce.application.port.`in`.report.command.GetLatestSessionReportCommand
import com.krince.reminisce.application.port.`in`.report.result.LatestSessionReportResult

interface GetLatestSessionReportUseCase {
    fun execute(command: GetLatestSessionReportCommand): LatestSessionReportResult
}
