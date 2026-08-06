package com.krince.reminisce.infra.adapter.out.persistence.report.mapper

import com.krince.reminisce.domain.model.report.Report
import com.krince.reminisce.domain.model.report.vo.ReportId
import com.krince.reminisce.domain.model.speakingsession.vo.SpeakingSessionId
import com.krince.reminisce.infra.adapter.out.persistence.report.entity.ReportOrmEntity

object ReportMapper {
    fun toDomain(ormEntity: ReportOrmEntity): Report = Report(
        reportId = ReportId(ormEntity.id),
        sessionId = SpeakingSessionId(ormEntity.sessionId),
        summary = ormEntity.summary,
        strengths = ormEntity.strengths,
        nextFocus = ormEntity.nextFocus,
        competencyAnalysis = ormEntity.competencyAnalysis,
        representativeUtterance = ormEntity.representativeUtterance,
        homeConversationGuide = ormEntity.homeConversationGuide,
        createdAt = ormEntity.createdAt,
    )

    fun toEntity(domain: Report): ReportOrmEntity = ReportOrmEntity(
        id = domain.reportId.value,
        sessionId = domain.sessionId.value,
        summary = domain.summary,
        strengths = domain.strengths,
        nextFocus = domain.nextFocus,
        competencyAnalysis = domain.competencyAnalysis,
        representativeUtterance = domain.representativeUtterance,
        homeConversationGuide = domain.homeConversationGuide,
        createdAt = domain.createdAt,
    )
}
