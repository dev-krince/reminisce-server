package com.krince.reminisce.infra.adapter.out.persistence.report.converter

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.krince.reminisce.domain.model.report.CompetencyAnalysis
import com.krince.reminisce.domain.model.report.CompetencyItem
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class CompetencyAnalysisConverter : AttributeConverter<CompetencyAnalysis?, String?> {
    private val objectMapper: ObjectMapper = ObjectMapper()

    override fun convertToDatabaseColumn(attribute: CompetencyAnalysis?): String? {
        if (attribute == null) {
            return null
        }

        val objectNode: ObjectNode = objectMapper.createObjectNode()
        objectNode.set<ObjectNode>(VOCABULARY_FIELD, itemNode(attribute.vocabulary))
        objectNode.set<ObjectNode>(PERSPECTIVE_EMPATHY_FIELD, itemNode(attribute.perspectiveEmpathy))
        objectNode.set<ObjectNode>(EMOTION_FIELD, itemNode(attribute.emotion))
        objectNode.set<ObjectNode>(INTERACTION_FIELD, itemNode(attribute.interaction))
        objectNode.set<ObjectNode>(THOUGHT_REASON_FIELD, itemNode(attribute.thoughtReason))
        objectNode.set<ObjectNode>(RESULT_SOLUTION_FIELD, itemNode(attribute.resultSolution))

        return objectMapper.writeValueAsString(objectNode)
    }

    override fun convertToEntityAttribute(dbData: String?): CompetencyAnalysis? {
        if (dbData == null) {
            return null
        }

        val root: JsonNode = objectMapper.readTree(dbData)

        return CompetencyAnalysis(
            vocabulary = readItem(root, VOCABULARY_FIELD),
            perspectiveEmpathy = readItem(root, PERSPECTIVE_EMPATHY_FIELD),
            emotion = readItem(root, EMOTION_FIELD),
            interaction = readItem(root, INTERACTION_FIELD),
            thoughtReason = readItem(root, THOUGHT_REASON_FIELD),
            resultSolution = readItem(root, RESULT_SOLUTION_FIELD),
        )
    }

    private fun itemNode(item: CompetencyItem): ObjectNode {
        val node: ObjectNode = objectMapper.createObjectNode()
        node.put(LABEL_FIELD, item.label)
        node.put(FEATURE_FIELD, item.feature)
        putNullable(node, EVIDENCE_UTTERANCE_FIELD, item.evidenceUtterance)
        node.put(STRENGTH_FIELD, item.strength)
        node.put(IMPROVEMENT_FIELD, item.improvement)

        return node
    }

    private fun putNullable(node: ObjectNode, field: String, value: String?) {
        if (value == null) {
            node.putNull(field)
            return
        }

        node.put(field, value)
    }

    private fun readItem(root: JsonNode, field: String): CompetencyItem {
        val node: JsonNode = root.get(field)

        return CompetencyItem(
            label = node.get(LABEL_FIELD).asText(),
            feature = node.get(FEATURE_FIELD).asText(),
            evidenceUtterance = readNullable(node, EVIDENCE_UTTERANCE_FIELD),
            strength = node.get(STRENGTH_FIELD).asText(),
            improvement = node.get(IMPROVEMENT_FIELD).asText(),
        )
    }

    private fun readNullable(node: JsonNode, field: String): String? {
        val value: JsonNode = node.get(field)
        if (value.isNull) {
            return null
        }

        return value.asText()
    }

    companion object {
        private const val VOCABULARY_FIELD: String = "vocabulary"
        private const val PERSPECTIVE_EMPATHY_FIELD: String = "perspectiveEmpathy"
        private const val EMOTION_FIELD: String = "emotion"
        private const val INTERACTION_FIELD: String = "interaction"
        private const val THOUGHT_REASON_FIELD: String = "thoughtReason"
        private const val RESULT_SOLUTION_FIELD: String = "resultSolution"
        private const val LABEL_FIELD: String = "label"
        private const val FEATURE_FIELD: String = "feature"
        private const val EVIDENCE_UTTERANCE_FIELD: String = "evidenceUtterance"
        private const val STRENGTH_FIELD: String = "strength"
        private const val IMPROVEMENT_FIELD: String = "improvement"
    }
}
