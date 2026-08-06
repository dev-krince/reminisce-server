package com.krince.reminisce.infra.adapter.out.persistence.notice

import com.krince.reminisce.infra.adapter.out.persistence.notice.entity.NoticeOrmEntity
import org.springframework.data.jpa.repository.JpaRepository

interface NoticeRepository : JpaRepository<NoticeOrmEntity, String> {
    fun findAllByStatusOrderByCreatedDateDesc(status: String): List<NoticeOrmEntity>

    fun findByNoticeIdAndStatus(noticeId: String, status: String): NoticeOrmEntity?
}
