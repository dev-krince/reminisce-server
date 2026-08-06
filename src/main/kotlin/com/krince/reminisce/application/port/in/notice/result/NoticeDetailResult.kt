package com.krince.reminisce.application.port.`in`.notice.result

import com.krince.reminisce.domain.model.notice.Notice
import java.time.LocalDateTime

class NoticeDetailResult(
    val noticeId: String,
    val title: String,
    val content: String,
    val createdAt: LocalDateTime?,
) {
    companion object {
        fun from(notice: Notice): NoticeDetailResult = NoticeDetailResult(
            noticeId = notice.noticeId.value,
            title = notice.title,
            content = notice.content,
            createdAt = notice.createdAt,
        )
    }
}
