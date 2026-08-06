package com.krince.reminisce.infra.adapter.out.persistence.utteranceanalysis.converter

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.krince.reminisce.domain.model.story.vo.ThinkingElement
import com.krince.reminisce.domain.model.utteranceanalysis.DetectedElement
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class DetectedElementsConverter : AttributeConverter<List<DetectedElement>?, String?> {
    private val objectMapper: ObjectMapper = ObjectMapper()

    override fun convertToDatabaseColumn(attribute: List<DetectedElement>?): String? {
        if (attribute == null) {
            return null
        }

        val arrayNode: ArrayNode = objectMapper.createArrayNode()
        attribute.forEach { element ->
            val objectNode = arrayNode.addObject()
            objectNode.put(TYPE_FIELD, element.type.name)
            objectNode.put(EVIDENCE_FIELD, element.evidence)
        }

        return objectMapper.writeValueAsString(arrayNode)
    }

    override fun convertToEntityAttribute(dbData: String?): List<DetectedElement>? {
        if (dbData == null) {
            return null
        }

        return objectMapper.readTree(dbData).map { node ->
            DetectedElement(
                type = ThinkingElement.valueOf(node.get(TYPE_FIELD).asText()),
                evidence = node.get(EVIDENCE_FIELD).asText(),
            )
        }
    }

    companion object {
        private const val TYPE_FIELD: String = "type"
        private const val EVIDENCE_FIELD: String = "evidence"
    }
}
