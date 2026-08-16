package com.krince.reminisce.infra.adapter.`in`.dto.report.response

import com.fasterxml.jackson.annotation.JsonFormat
import com.krince.reminisce.application.port.`in`.report.result.SessionReportResult
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(title = "SessionReportResponse", description = "말하기 세션 보호자 리포트 응답")
class SessionReportResponse(
    @field:Schema(description = "리포트 총평 한 문장", required = true)
    val summary: String,

    @field:Schema(description = "대표 발화와 선정 이유", required = true)
    val representativeUtterance: RepresentativeUtteranceResponse,

    @field:Schema(description = "가정 연계 대화 가이드", required = true)
    val homeConversationGuide: HomeConversationGuideResponse,

    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @field:Schema(description = "리포트 생성 시각", example = "2026-01-09 14:30:25", required = true)
    val createdAt: LocalDateTime,
)

@Schema(title = "RepresentativeUtteranceResponse", description = "대표 발화")
class RepresentativeUtteranceResponse(
    @field:Schema(description = "대표로 선정된 아이 발화", required = false)
    val text: String?,

    @field:Schema(description = "선정 이유", required = true)
    val reason: String,
)

@Schema(title = "HomeConversationGuideResponse", description = "가정 연계 대화 가이드")
class HomeConversationGuideResponse(
    @field:Schema(description = "이야기 주제 이어가기 질문", required = true)
    val storyThemeQuestions: List<String>,

    @field:Schema(description = "일상생활 연결 질문", required = true)
    val dailyLifeQuestions: List<String>,
)

fun sessionReportResponse(result: SessionReportResult): SessionReportResponse = SessionReportResponse(
    summary = result.overall.headline,
    representativeUtterance = RepresentativeUtteranceResponse(
        text = result.representative.text,
        reason = result.representative.reason,
    ),
    homeConversationGuide = HomeConversationGuideResponse(
        storyThemeQuestions = result.homeGuide.storyQuestions.map { it.question },
        dailyLifeQuestions = result.homeGuide.dailyQuestions.map { it.question },
    ),
    createdAt = result.createdAt,
)
