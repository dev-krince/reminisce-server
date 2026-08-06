package com.krince.reminisce.infra.adapter.out.persistence.story.converter

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.krince.reminisce.domain.model.story.Mission
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class MissionConverter : AttributeConverter<Mission?, String?> {
    private val objectMapper: ObjectMapper = ObjectMapper()

    override fun convertToDatabaseColumn(attribute: Mission?): String? {
        if (attribute == null) {
            return null
        }

        val rootNode: ObjectNode = objectMapper.createObjectNode()
        rootNode.put(GOAL_FIELD, attribute.goal)
        val examplesNode = rootNode.putArray(EXAMPLES_FIELD)
        attribute.examples.forEach { examplesNode.add(it) }

        return objectMapper.writeValueAsString(rootNode)
    }

    override fun convertToEntityAttribute(dbData: String?): Mission? {
        if (dbData == null) {
            return null
        }

        val rootNode: JsonNode = objectMapper.readTree(dbData)
        val goal: String = rootNode.path(GOAL_FIELD).asText()
        val examples: List<String> = rootNode.path(EXAMPLES_FIELD).map { it.asText() }

        return Mission(goal = goal, examples = examples)
    }

    private companion object {
        const val GOAL_FIELD = "goal"
        const val EXAMPLES_FIELD = "examples"
    }
}
