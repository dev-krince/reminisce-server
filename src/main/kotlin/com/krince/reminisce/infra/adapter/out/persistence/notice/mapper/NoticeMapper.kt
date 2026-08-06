package com.krince.reminisce.infra.adapter.out.persistence.notice.mapper

import com.krince.reminisce.domain.model.notice.Notice
import com.krince.reminisce.domain.model.notice.vo.NoticeId
import com.krince.reminisce.domain.model.notice.vo.NoticeStatus
import com.krince.reminisce.infra.adapter.out.persistence.notice.entity.NoticeOrmEntity

object NoticeMapper {
    fun toDomain(entity: NoticeOrmEntity): Notice = Notice(
        noticeId = NoticeId(entity.noticeId),
        title = entity.title,
        content = entity.content,
        status = NoticeStatus.valueOf(entity.status),
        createdAt = entity.createdDate,
    )
}
