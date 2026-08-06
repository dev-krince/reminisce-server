package com.krince.reminisce.infra.adapter.out.persistence.utteranceanalysis

import com.krince.reminisce.application.port.out.utteranceanalysis.CommandUtteranceAnalysisPort
import com.krince.reminisce.domain.model.utteranceanalysis.UtteranceAnalysis
import com.krince.reminisce.infra.adapter.out.persistence.utteranceanalysis.entity.UtteranceAnalysisOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.utteranceanalysis.mapper.UtteranceAnalysisMapper
import org.springframework.stereotype.Component

@Component
class UtteranceAnalysisOrmAdapter(
    private val repository: UtteranceAnalysisRepository,
) : CommandUtteranceAnalysisPort {

    override fun save(analysis: UtteranceAnalysis): UtteranceAnalysis {
        val ormEntity: UtteranceAnalysisOrmEntity = UtteranceAnalysisMapper.toEntity(analysis)
        val savedEntity: UtteranceAnalysisOrmEntity = repository.saveAndFlush(ormEntity)

        return UtteranceAnalysisMapper.toDomain(savedEntity)
    }
}
