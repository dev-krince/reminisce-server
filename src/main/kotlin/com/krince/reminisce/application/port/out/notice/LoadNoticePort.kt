package com.krince.reminisce.application.port.out.notice

import com.krince.reminisce.domain.model.notice.Notice
import com.krince.reminisce.domain.model.notice.vo.NoticeId

interface LoadNoticePort {
    fun findAllPublished(): List<Notice>

    fun findByIdPublished(noticeId: NoticeId): Notice?
}
