package com.krince.reminisce.infra.adapter.out.persistence.notice

import com.krince.reminisce.application.port.out.notice.LoadNoticePort
import com.krince.reminisce.domain.model.notice.Notice
import com.krince.reminisce.domain.model.notice.vo.NoticeId
import com.krince.reminisce.domain.model.notice.vo.NoticeStatus
import com.krince.reminisce.infra.adapter.out.persistence.notice.mapper.NoticeMapper
import org.springframework.stereotype.Component

@Component
class NoticeOrmAdapter(
    private val noticeRepository: NoticeRepository,
) : LoadNoticePort {

    override fun findAllPublished(): List<Notice> =
        noticeRepository.findAllByStatusOrderByCreatedDateDesc(NoticeStatus.PUBLISHED.name)
            .map { NoticeMapper.toDomain(it) }

    override fun findByIdPublished(noticeId: NoticeId): Notice? =
        noticeRepository.findByNoticeIdAndStatus(noticeId.value, NoticeStatus.PUBLISHED.name)
            ?.let { NoticeMapper.toDomain(it) }
}
