package com.krince.reminisce.testutil.fixture

import com.krince.reminisce.infra.adapter.out.persistence.utteranceanalysis.UtteranceAnalysisRepository
import com.krince.reminisce.infra.adapter.out.persistence.utteranceanalysis.entity.UtteranceAnalysisOrmEntity
import org.springframework.stereotype.Component

@Component
class TestUtteranceAnalysisFixture(
    private val utteranceAnalysisRepository: UtteranceAnalysisRepository,
) {
    fun count(): Long = utteranceAnalysisRepository.count()

    fun findAll(): List<UtteranceAnalysisOrmEntity> = utteranceAnalysisRepository.findAll()

    fun deleteAllBatch() {
        utteranceAnalysisRepository.deleteAllInBatch()
    }
}
