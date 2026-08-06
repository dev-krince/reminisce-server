package com.krince.reminisce.application.port.`in`.notice.result

import com.krince.reminisce.domain.model.notice.Notice
import java.time.LocalDateTime

class NoticeSummaryResult(
    val noticeId: String,
    val title: String,
    val createdAt: LocalDateTime?,
) {
    companion object {
        fun from(notice: Notice): NoticeSummaryResult = NoticeSummaryResult(
            noticeId = notice.noticeId.value,
            title = notice.title,
            createdAt = notice.createdAt,
        )
    }
}
