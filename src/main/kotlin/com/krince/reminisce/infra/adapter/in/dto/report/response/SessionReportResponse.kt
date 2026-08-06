package com.krince.reminisce.infra.adapter.`in`.dto.report.response

import com.fasterxml.jackson.annotation.JsonFormat
import com.krince.reminisce.application.port.`in`.report.result.SessionReportResult
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(title = "SessionReportResponse", description = "말하기 세션 보호자 리포트 응답")
class SessionReportResponse(
    @field:Schema(description = "스텁 생성 요약문", required = true)
    val summary: String,

    @field:Schema(description = "세션에서 확인된 사고 요소 강점", example = "[\"EMOTION\", \"PERSPECTIVE\"]", required = true)
    val strengths: List<String>,

    @field:Schema(description = "아직 보여주지 않은 다음 초점 사고 요소", example = "[\"DECISION\", \"REASON\"]", required = true)
    val nextFocus: List<String>,

    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @field:Schema(description = "리포트 생성 시각", example = "2026-01-09 14:30:25", required = true)
    val createdAt: LocalDateTime,
)

fun sessionReportResponse(result: SessionReportResult): SessionReportResponse = SessionReportResponse(
    summary = result.summary,
    strengths = result.strengths.map { it.name },
    nextFocus = result.nextFocus.map { it.name },
    createdAt = result.createdAt,
)
