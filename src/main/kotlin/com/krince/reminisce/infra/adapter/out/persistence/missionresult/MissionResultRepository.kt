package com.krince.reminisce.infra.adapter.out.persistence.missionresult

import com.krince.reminisce.infra.adapter.out.persistence.missionresult.entity.MissionResultOrmEntity
import org.springframework.data.jpa.repository.JpaRepository

interface MissionResultRepository : JpaRepository<MissionResultOrmEntity, String> {
    fun findBySessionIdAndSceneId(sessionId: String, sceneId: String): MissionResultOrmEntity?

    fun deleteAllBySessionIdIn(sessionIds: List<String>)
}
