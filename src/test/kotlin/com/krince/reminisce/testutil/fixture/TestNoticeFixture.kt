package com.krince.reminisce.testutil.fixture

import com.krince.reminisce.infra.adapter.out.persistence.notice.NoticeRepository
import com.krince.reminisce.infra.adapter.out.persistence.notice.entity.NoticeOrmEntity
import org.springframework.stereotype.Component

@Component
class TestNoticeFixture(
    private val noticeRepository: NoticeRepository,
) {
    fun saveNotice(entity: NoticeOrmEntity): NoticeOrmEntity = noticeRepository.save(entity)

    fun deleteAllBatch() {
        noticeRepository.deleteAllInBatch()
    }
}
