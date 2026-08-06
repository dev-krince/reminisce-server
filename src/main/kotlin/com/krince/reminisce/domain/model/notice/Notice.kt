package com.krince.reminisce.domain.model.notice

import com.krince.reminisce.domain.model.notice.vo.NoticeId
import com.krince.reminisce.domain.model.notice.vo.NoticeStatus
import java.time.LocalDateTime

class Notice(
    val noticeId: NoticeId,
    val title: String,
    val content: String,
    val status: NoticeStatus,
    val createdAt: LocalDateTime?,
)
