package com.krince.reminisce.infra.adapter.`in`.dto.report.response

import com.krince.reminisce.application.port.`in`.report.result.LatestSessionReportResult
import io.swagger.v3.oas.annotations.media.Schema

@Schema(title = "LatestSessionReportResponse", description = "최신 완료 세션 리포트 응답")
class LatestSessionReportResponse(
    @field:Schema(description = "리포트 기준이 된 세션 고유 식별자 (가장 최근에 완료된 세션)", required = true)
    val sessionId: String,

    @field:Schema(description = "세션 리포트 (5개 탭)", required = true)
    val report: SessionReportResponse,
)

fun latestSessionReportResponse(result: LatestSessionReportResult): LatestSessionReportResponse =
    LatestSessionReportResponse(
        sessionId = result.sessionId,
        report = sessionReportResponse(result.report),
    )
