package com.krince.reminisce.testutil.fixture

import com.krince.reminisce.infra.adapter.out.persistence.report.ReportRepository
import com.krince.reminisce.infra.adapter.out.persistence.report.entity.ReportOrmEntity
import org.springframework.stereotype.Component

@Component
class TestReportFixture(
    private val reportRepository: ReportRepository,
) {
    fun save(entity: ReportOrmEntity): ReportOrmEntity = reportRepository.save(entity)

    fun count(): Long = reportRepository.count()

    fun findBySessionId(sessionId: String): ReportOrmEntity? = reportRepository.findBySessionId(sessionId)

    fun deleteAllBatch() {
        reportRepository.deleteAllInBatch()
    }
}
