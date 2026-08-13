package com.krince.reminisce.infra.adapter.out.persistence.postactivityresult.converter

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class RetellingSegmentsConverter : AttributeConverter<List<String>?, String?> {
    private val objectMapper: ObjectMapper = ObjectMapper()

    override fun convertToDatabaseColumn(attribute: List<String>?): String? {
        if (attribute == null) {
            return null
        }

        val arrayNode: ArrayNode = objectMapper.createArrayNode()
        attribute.forEach { arrayNode.add(it) }

        return objectMapper.writeValueAsString(arrayNode)
    }

    override fun convertToEntityAttribute(dbData: String?): List<String>? {
        if (dbData == null) {
            return null
        }

        return objectMapper.readTree(dbData).map { it.asText() }
    }
}
