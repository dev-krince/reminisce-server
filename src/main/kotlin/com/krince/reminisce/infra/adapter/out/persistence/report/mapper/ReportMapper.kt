package com.krince.reminisce.infra.adapter.out.persistence.report.mapper

import com.krince.reminisce.domain.model.report.Report
import com.krince.reminisce.domain.model.report.vo.ReportId
import com.krince.reminisce.domain.model.speakingsession.vo.SpeakingSessionId
import com.krince.reminisce.infra.adapter.out.persistence.report.entity.ReportOrmEntity

object ReportMapper {
    fun toDomain(ormEntity: ReportOrmEntity): Report? {
        val overall = ormEntity.overall ?: return null
        val participation = ormEntity.participation ?: return null
        val speechAnalyses = ormEntity.speechAnalyses ?: return null
        val sceneHighlights = ormEntity.sceneHighlights ?: return null
        val representativeUtterance = ormEntity.representativeUtterance ?: return null
        val homeGuide = ormEntity.homeGuide ?: return null

        return Report(
            reportId = ReportId(ormEntity.id),
            sessionId = SpeakingSessionId(ormEntity.sessionId),
            overall = overall,
            participation = participation,
            speechAnalyses = speechAnalyses,
            sceneHighlights = sceneHighlights,
            representative = representativeUtterance,
            homeGuide = homeGuide,
            createdAt = ormEntity.createdAt,
        )
    }

    fun toEntity(domain: Report, id: String = domain.reportId.value): ReportOrmEntity = ReportOrmEntity(
        id = id,
        sessionId = domain.sessionId.value,
        overall = domain.overall,
        participation = domain.participation,
        speechAnalyses = domain.speechAnalyses,
        sceneHighlights = domain.sceneHighlights,
        representativeUtterance = domain.representative,
        homeGuide = domain.homeGuide,
        createdAt = domain.createdAt,
    )
}
