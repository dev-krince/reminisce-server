package com.krince.reminisce.infra.adapter.out.persistence.report

import com.krince.reminisce.application.port.out.report.CommandReportPort
import com.krince.reminisce.application.port.out.report.LoadReportPort
import com.krince.reminisce.domain.model.report.Report
import com.krince.reminisce.domain.model.speakingsession.vo.SpeakingSessionId
import com.krince.reminisce.infra.adapter.out.persistence.report.entity.ReportOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.report.mapper.ReportMapper
import org.springframework.stereotype.Component

@Component
class ReportOrmAdapter(
    private val repository: ReportRepository,
) : CommandReportPort, LoadReportPort {

    override fun save(report: Report): Report {
        val ormEntity: ReportOrmEntity = ReportMapper.toEntity(report)
        val savedEntity: ReportOrmEntity = repository.saveAndFlush(ormEntity)

        return ReportMapper.toDomain(savedEntity)
    }

    override fun findBySession(sessionId: SpeakingSessionId): Report? {
        val ormEntity: ReportOrmEntity = repository.findBySessionId(sessionId.value) ?: return null

        return ReportMapper.toDomain(ormEntity)
    }
}
