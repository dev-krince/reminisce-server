package com.krince.reminisce.infra.adapter.out.persistence.appconfig

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.krince.reminisce.application.port.out.profileinterview.InterviewTurnSettingsPort
import com.krince.reminisce.domain.model.profileinterview.vo.InterviewStage
import com.krince.reminisce.infra.adapter.out.persistence.appconfig.entity.AppConfigOrmEntity
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class InterviewTurnSettingsAdapter(
    private val repository: AppConfigRepository,
) : InterviewTurnSettingsPort {

    private val objectMapper: ObjectMapper = jacksonObjectMapper()

    override fun load(): Map<InterviewStage, Int> {
        val entity: AppConfigOrmEntity = repository.findByIdOrNull(CONFIG_KEY) ?: return emptyMap()
        val rawTurns: Map<String, Int> = objectMapper.readValue(entity.configValue)

        return rawTurns.mapNotNull { (name, turns) ->
            InterviewStage.entries.find { it.name == name }?.let { it to turns }
        }.toMap()
    }

    override fun save(stageTurns: Map<InterviewStage, Int>) {
        val json: String = objectMapper.writeValueAsString(stageTurns.mapKeys { it.key.name })
        val existing: AppConfigOrmEntity? = repository.findByIdOrNull(CONFIG_KEY)
        val entity = AppConfigOrmEntity(configKey = CONFIG_KEY, configValue = json)
        if (existing != null) {
            entity.createdDate = existing.createdDate
        }
        repository.saveAndFlush(entity)
    }

    private companion object {
        const val CONFIG_KEY = "interview_stage_turns"
    }
}
