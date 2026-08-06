package com.krince.reminisce.infra.adapter.out.persistence.utteranceanalysis

import com.krince.reminisce.application.port.out.utteranceanalysis.CommandUtteranceAnalysisPort
import com.krince.reminisce.application.port.out.utteranceanalysis.LoadUtteranceAnalysisPort
import com.krince.reminisce.domain.model.message.vo.MessageId
import com.krince.reminisce.domain.model.utteranceanalysis.UtteranceAnalysis
import com.krince.reminisce.infra.adapter.out.persistence.utteranceanalysis.entity.UtteranceAnalysisOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.utteranceanalysis.mapper.UtteranceAnalysisMapper
import org.springframework.stereotype.Component

@Component
class UtteranceAnalysisOrmAdapter(
    private val repository: UtteranceAnalysisRepository,
) : CommandUtteranceAnalysisPort, LoadUtteranceAnalysisPort {

    override fun save(analysis: UtteranceAnalysis): UtteranceAnalysis {
        val ormEntity: UtteranceAnalysisOrmEntity = UtteranceAnalysisMapper.toEntity(analysis)
        val savedEntity: UtteranceAnalysisOrmEntity = repository.saveAndFlush(ormEntity)

        return UtteranceAnalysisMapper.toDomain(savedEntity)
    }

    override fun findByMessageIds(messageIds: List<MessageId>): List<UtteranceAnalysis> {
        if (messageIds.isEmpty()) {
            return emptyList()
        }

        val entities: List<UtteranceAnalysisOrmEntity> =
            repository.findByMessageIdIn(messageIds.map { it.value })

        return entities.map { UtteranceAnalysisMapper.toDomain(it) }
    }

    override fun deleteAllByMessageIds(messageIds: List<String>) {
        if (messageIds.isEmpty()) {
            return
        }

        repository.deleteAllByMessageIdIn(messageIds)
    }
}
