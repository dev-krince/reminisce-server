package com.krince.reminisce.infra.adapter.out.persistence.utteranceanalysis

import com.krince.reminisce.infra.adapter.out.persistence.utteranceanalysis.entity.UtteranceAnalysisOrmEntity
import org.springframework.data.jpa.repository.JpaRepository

interface UtteranceAnalysisRepository : JpaRepository<UtteranceAnalysisOrmEntity, String> {
    fun findByMessageIdIn(messageIds: List<String>): List<UtteranceAnalysisOrmEntity>
}
