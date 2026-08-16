package com.krince.reminisce.infra.adapter.out.persistence.missionresult.mapper

import com.krince.reminisce.domain.model.missionresult.MissionResult
import com.krince.reminisce.domain.model.missionresult.vo.MissionResultId
import com.krince.reminisce.domain.model.speakingsession.vo.SpeakingSessionId
import com.krince.reminisce.infra.adapter.out.persistence.missionresult.entity.MissionResultOrmEntity

object MissionResultMapper {
    fun toDomain(ormEntity: MissionResultOrmEntity): MissionResult = MissionResult(
        id = MissionResultId(ormEntity.id),
        sessionId = SpeakingSessionId(ormEntity.sessionId),
        sceneId = ormEntity.sceneId,
        completed = ormEntity.completed,
        attemptCount = ormEntity.attemptCount,
        completedAt = ormEntity.completedAt,
        createdDate = ormEntity.createdDate,
        modifiedDate = ormEntity.modifiedDate,
    )

    fun toEntity(domain: MissionResult): MissionResultOrmEntity = MissionResultOrmEntity(
        id = domain.id.value,
        sessionId = domain.sessionId.value,
        sceneId = domain.sceneId,
        completed = domain.completed,
        attemptCount = domain.attemptCount,
        completedAt = domain.completedAt,
    ).apply {
        createdDate = domain.createdDate
        modifiedDate = domain.modifiedDate
    }
}
