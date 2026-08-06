package com.krince.reminisce.infra.adapter.out.persistence.utteranceanalysis.mapper

import com.krince.reminisce.domain.model.message.vo.MessageId
import com.krince.reminisce.domain.model.utteranceanalysis.UtteranceAnalysis
import com.krince.reminisce.domain.model.utteranceanalysis.vo.AnalysisId
import com.krince.reminisce.domain.model.utteranceanalysis.vo.ChildIntent
import com.krince.reminisce.domain.model.utteranceanalysis.vo.UtteranceValidity
import com.krince.reminisce.infra.adapter.out.persistence.utteranceanalysis.entity.UtteranceAnalysisOrmEntity

object UtteranceAnalysisMapper {
    fun toDomain(ormEntity: UtteranceAnalysisOrmEntity): UtteranceAnalysis = UtteranceAnalysis(
        analysisId = AnalysisId(ormEntity.id),
        messageId = MessageId(ormEntity.messageId),
        childIntent = ChildIntent.valueOf(ormEntity.childIntent),
        mainPoint = ormEntity.mainPoint,
        detectedElements = ormEntity.detectedElements,
        validity = UtteranceValidity.valueOf(ormEntity.utteranceValidity),
    )

    fun toEntity(domain: UtteranceAnalysis): UtteranceAnalysisOrmEntity = UtteranceAnalysisOrmEntity(
        id = domain.analysisId.value,
        messageId = domain.messageId.value,
        childIntent = domain.childIntent.name,
        mainPoint = domain.mainPoint,
        detectedElements = domain.detectedElements,
        utteranceValidity = domain.validity.name,
    )
}
