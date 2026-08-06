package com.krince.reminisce.infra.adapter.out.persistence.report.converter

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.krince.reminisce.domain.model.report.HomeConversationGuide
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class HomeConversationGuideConverter : AttributeConverter<HomeConversationGuide?, String?> {
    private val objectMapper: ObjectMapper = ObjectMapper()

    override fun convertToDatabaseColumn(attribute: HomeConversationGuide?): String? {
        if (attribute == null) {
            return null
        }

        val node: ObjectNode = objectMapper.createObjectNode()
        node.set<ArrayNode>(STORY_THEME_FIELD, textArray(attribute.storyThemeQuestions))
        node.set<ArrayNode>(DAILY_LIFE_FIELD, textArray(attribute.dailyLifeQuestions))

        return objectMapper.writeValueAsString(node)
    }

    override fun convertToEntityAttribute(dbData: String?): HomeConversationGuide? {
        if (dbData == null) {
            return null
        }

        val node: JsonNode = objectMapper.readTree(dbData)

        return HomeConversationGuide(
            storyThemeQuestions = readTextList(node, STORY_THEME_FIELD),
            dailyLifeQuestions = readTextList(node, DAILY_LIFE_FIELD),
        )
    }

    private fun textArray(values: List<String>): ArrayNode {
        val arrayNode: ArrayNode = objectMapper.createArrayNode()
        values.forEach { arrayNode.add(it) }

        return arrayNode
    }

    private fun readTextList(node: JsonNode, field: String): List<String> =
        node.get(field).map { it.asText() }

    companion object {
        private const val STORY_THEME_FIELD: String = "storyThemeQuestions"
        private const val DAILY_LIFE_FIELD: String = "dailyLifeQuestions"
    }
}
