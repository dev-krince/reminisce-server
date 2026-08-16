package com.krince.reminisce.testutil.fixture

import com.krince.reminisce.infra.adapter.out.persistence.missionresult.MissionResultRepository
import com.krince.reminisce.infra.adapter.out.persistence.missionresult.entity.MissionResultOrmEntity
import org.springframework.stereotype.Component

@Component
class TestMissionResultFixture(
    private val missionResultRepository: MissionResultRepository,
) {
    fun findBySessionIdAndSceneId(sessionId: String, sceneId: String): MissionResultOrmEntity? =
        missionResultRepository.findBySessionIdAndSceneId(sessionId, sceneId)

    fun count(): Long = missionResultRepository.count()

    fun deleteAllBatch() {
        missionResultRepository.deleteAllInBatch()
    }
}
