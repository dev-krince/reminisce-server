package com.krince.reminisce.infra.adapter.out.persistence.report.converter

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.krince.reminisce.domain.model.story.vo.ThinkingElement
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class ThinkingElementsConverter : AttributeConverter<List<ThinkingElement>?, String?> {
    private val objectMapper: ObjectMapper = ObjectMapper()

    override fun convertToDatabaseColumn(attribute: List<ThinkingElement>?): String? {
        if (attribute == null) {
            return null
        }

        val arrayNode: ArrayNode = objectMapper.createArrayNode()
        attribute.forEach { arrayNode.add(it.name) }

        return objectMapper.writeValueAsString(arrayNode)
    }

    override fun convertToEntityAttribute(dbData: String?): List<ThinkingElement>? {
        if (dbData == null) {
            return null
        }

        return objectMapper.readTree(dbData).map { ThinkingElement.valueOf(it.asText()) }
    }
}
