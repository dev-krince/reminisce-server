package com.krince.reminisce.application.port.out.report

import com.krince.reminisce.domain.model.report.Report

interface CommandReportPort {
    fun save(report: Report): Report
}
