package com.krince.reminisce.infra.adapter.out.persistence.report

import com.krince.reminisce.infra.adapter.out.persistence.report.entity.ReportOrmEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ReportRepository : JpaRepository<ReportOrmEntity, String> {
    fun findBySessionId(sessionId: String): ReportOrmEntity?
}
