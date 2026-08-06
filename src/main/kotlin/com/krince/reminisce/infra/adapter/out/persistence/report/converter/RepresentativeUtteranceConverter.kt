package com.krince.reminisce.infra.adapter.out.persistence.report.converter

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.krince.reminisce.domain.model.report.RepresentativeUtterance
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class RepresentativeUtteranceConverter : AttributeConverter<RepresentativeUtterance?, String?> {
    private val objectMapper: ObjectMapper = ObjectMapper()

    override fun convertToDatabaseColumn(attribute: RepresentativeUtterance?): String? {
        if (attribute == null) {
            return null
        }

        val node: ObjectNode = objectMapper.createObjectNode()
        putNullableText(node, attribute.text)
        node.put(REASON_FIELD, attribute.reason)

        return objectMapper.writeValueAsString(node)
    }

    override fun convertToEntityAttribute(dbData: String?): RepresentativeUtterance? {
        if (dbData == null) {
            return null
        }

        val node: JsonNode = objectMapper.readTree(dbData)

        return RepresentativeUtterance(
            text = readNullableText(node),
            reason = node.get(REASON_FIELD).asText(),
        )
    }

    private fun putNullableText(node: ObjectNode, value: String?) {
        if (value == null) {
            node.putNull(TEXT_FIELD)
            return
        }

        node.put(TEXT_FIELD, value)
    }

    private fun readNullableText(node: JsonNode): String? {
        val value: JsonNode = node.get(TEXT_FIELD)
        if (value.isNull) {
            return null
        }

        return value.asText()
    }

    companion object {
        private const val TEXT_FIELD: String = "text"
        private const val REASON_FIELD: String = "reason"
    }
}
