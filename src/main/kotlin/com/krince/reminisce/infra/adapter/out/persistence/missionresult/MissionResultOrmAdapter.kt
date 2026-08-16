package com.krince.reminisce.infra.adapter.out.persistence.missionresult

import com.krince.reminisce.application.port.out.missionresult.CommandMissionResultPort
import com.krince.reminisce.application.port.out.missionresult.LoadMissionResultPort
import com.krince.reminisce.domain.model.missionresult.MissionResult
import com.krince.reminisce.domain.model.speakingsession.vo.SpeakingSessionId
import com.krince.reminisce.infra.adapter.out.persistence.missionresult.entity.MissionResultOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.missionresult.mapper.MissionResultMapper
import org.springframework.stereotype.Component

@Component
class MissionResultOrmAdapter(
    private val repository: MissionResultRepository,
) : CommandMissionResultPort, LoadMissionResultPort {

    override fun save(result: MissionResult): MissionResult {
        val ormEntity: MissionResultOrmEntity = MissionResultMapper.toEntity(result)
        val savedEntity: MissionResultOrmEntity = repository.saveAndFlush(ormEntity)

        return MissionResultMapper.toDomain(savedEntity)
    }

    override fun findBySessionAndScene(sessionId: SpeakingSessionId, sceneId: String): MissionResult? {
        val ormEntity: MissionResultOrmEntity =
            repository.findBySessionIdAndSceneId(sessionId.value, sceneId) ?: return null

        return MissionResultMapper.toDomain(ormEntity)
    }
}
