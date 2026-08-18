package com.krince.reminisce.infra.adapter.out.persistence.story

import com.krince.reminisce.application.port.out.story.SceneTurnPort
import com.krince.reminisce.application.port.out.story.SceneTurns
import com.krince.reminisce.infra.adapter.out.persistence.story.entity.SceneOrmEntity
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class SceneTurnAdapter(
    private val sceneRepository: SceneRepository,
) : SceneTurnPort {

    override fun findTurns(sceneId: String): SceneTurns? {
        val entity: SceneOrmEntity = sceneRepository.findByIdOrNull(sceneId) ?: return null

        return SceneTurns(
            preferredTurns = entity.preferredTurns?.toInt(),
            maxTurns = entity.maxTurns?.toInt(),
        )
    }

    @Transactional
    override fun updateTurns(sceneId: String, preferredTurns: Int?, maxTurns: Int?) {
        sceneRepository.updateTurns(sceneId, preferredTurns?.toShort(), maxTurns?.toShort())
    }
}
