package com.krince.reminisce.testutil.fixture

import com.krince.reminisce.infra.adapter.out.persistence.report.ReportRepository
import com.krince.reminisce.infra.adapter.out.persistence.report.entity.ReportOrmEntity
import com.krince.reminisce.shared.util.UuidGenerator
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class TestReportFixture(
    private val reportRepository: ReportRepository,
) {
    fun save(entity: ReportOrmEntity): ReportOrmEntity = reportRepository.save(entity)

    fun saveLegacyStructureRow(sessionId: String): ReportOrmEntity = reportRepository.save(
        ReportOrmEntity(
            id = UuidGenerator.generate(),
            sessionId = sessionId,
            overall = null,
            participation = null,
            speechAnalyses = null,
            sceneHighlights = null,
            representativeUtterance = null,
            homeGuide = null,
            createdAt = LocalDateTime.now(),
        ),
    )

    fun count(): Long = reportRepository.count()

    fun findBySessionId(sessionId: String): ReportOrmEntity? = reportRepository.findBySessionId(sessionId)

    fun deleteAllBatch() {
        reportRepository.deleteAllInBatch()
    }
}
