package com.krince.reminisce.infra.adapter.out.persistence.story.converter

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.krince.reminisce.domain.model.story.Mission
import com.krince.reminisce.domain.model.story.vo.MissionType
import com.krince.reminisce.domain.model.story.vo.WordCard
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
        val examplesNode: ArrayNode = rootNode.putArray(EXAMPLES_FIELD)
        attribute.examples.forEach { examplesNode.add(it) }
        rootNode.put(TYPE_FIELD, attribute.type.name)
        writeWordCards(rootNode, attribute.wordCards)

        return objectMapper.writeValueAsString(rootNode)
    }

    override fun convertToEntityAttribute(dbData: String?): Mission? {
        if (dbData == null) {
            return null
        }

        val rootNode: JsonNode = objectMapper.readTree(dbData)
        val goal: String = rootNode.path(GOAL_FIELD).asText()
        val examples: List<String> = rootNode.path(EXAMPLES_FIELD).map { it.asText() }

        return Mission(
            goal = goal,
            examples = examples,
            type = readType(rootNode),
            wordCards = readWordCards(rootNode),
        )
    }

    private fun writeWordCards(rootNode: ObjectNode, wordCards: List<WordCard>?) {
        if (wordCards == null) {
            return
        }

        val wordCardsNode: ArrayNode = rootNode.putArray(WORD_CARDS_FIELD)
        wordCards.forEach { wordCard ->
            val wordCardNode: ObjectNode = wordCardsNode.addObject()
            wordCardNode.put(WORD_CARD_TEXT_FIELD, wordCard.text)
            wordCardNode.put(WORD_CARD_ORDER_FIELD, wordCard.correctOrder)
        }
    }

    private fun readType(rootNode: JsonNode): MissionType {
        val typeNode: JsonNode = rootNode.path(TYPE_FIELD)
        if (typeNode.isMissingNode || typeNode.asText().isBlank()) {
            return MissionType.SPEAKING
        }

        return MissionType.valueOf(typeNode.asText())
    }

    private fun readWordCards(rootNode: JsonNode): List<WordCard>? {
        val wordCardsNode: JsonNode = rootNode.path(WORD_CARDS_FIELD)
        if (wordCardsNode.isMissingNode || !wordCardsNode.isArray) {
            return null
        }

        return wordCardsNode.map { wordCardNode ->
            WordCard(
                text = wordCardNode.path(WORD_CARD_TEXT_FIELD).asText(),
                correctOrder = wordCardNode.path(WORD_CARD_ORDER_FIELD).asInt(),
            )
        }
    }

    private companion object {
        const val GOAL_FIELD = "goal"
        const val EXAMPLES_FIELD = "examples"
        const val TYPE_FIELD = "type"
        const val WORD_CARDS_FIELD = "wordCards"
        const val WORD_CARD_TEXT_FIELD = "text"
        const val WORD_CARD_ORDER_FIELD = "correctOrder"
    }
}
