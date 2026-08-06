package com.krince.reminisce.infra.adapter.`in`.dto.notice.response

import com.krince.reminisce.application.port.`in`.notice.result.NoticeDetailResult
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(title = "NoticeDetailResponse", description = "공지사항 상세 응답")
class NoticeDetailResponse(
    @field:Schema(description = "공지사항 고유 식별자", example = "notice-001", required = true)
    val noticeId: String,

    @field:Schema(description = "공지사항 제목", example = "서비스 점검 안내", required = true)
    val title: String,

    @field:Schema(description = "공지사항 내용", required = true)
    val content: String,

    @field:Schema(description = "공지사항 생성일시", required = false)
    val createdAt: LocalDateTime?,
)

fun noticeDetailResponse(result: NoticeDetailResult): NoticeDetailResponse = NoticeDetailResponse(
    noticeId = result.noticeId,
    title = result.title,
    content = result.content,
    createdAt = result.createdAt,
)
