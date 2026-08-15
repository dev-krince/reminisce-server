package com.krince.reminisce.infra.adapter.out.persistence.story.converter

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.krince.reminisce.domain.model.story.vo.PostActivityConfig
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class PostActivityConfigConverter : AttributeConverter<PostActivityConfig?, String?> {
    private val objectMapper: ObjectMapper = ObjectMapper()

    override fun convertToDatabaseColumn(attribute: PostActivityConfig?): String? {
        if (attribute == null) {
            return null
        }

        val rootNode: ObjectNode = objectMapper.createObjectNode()
        val cardsNode = rootNode.putArray(CARDS_FIELD)
        attribute.cards.forEach { card ->
            val cardNode: ObjectNode = cardsNode.addObject()
            cardNode.put(CARD_ID_FIELD, card.id)
            cardNode.put(CARD_TEXT_FIELD, card.text)
            cardNode.put(CARD_CORRECT_ORDER_FIELD, card.correctOrder)
            card.imageUrl?.let { cardNode.put(CARD_IMAGE_URL_FIELD, it) }
        }
        val retellingKeywordsNode = rootNode.putArray(RETELLING_KEYWORDS_FIELD)
        attribute.retellingKeywords.forEach { retellingKeywordsNode.add(it) }

        return objectMapper.writeValueAsString(rootNode)
    }

    override fun convertToEntityAttribute(dbData: String?): PostActivityConfig? {
        if (dbData == null) {
            return null
        }

        val rootNode: JsonNode = objectMapper.readTree(dbData)
        val cards: List<PostActivityConfig.Card> = rootNode.path(CARDS_FIELD).map { toCard(it) }
        val retellingKeywords: List<String> = rootNode.path(RETELLING_KEYWORDS_FIELD).map { it.asText() }

        return PostActivityConfig(cards = cards, retellingKeywords = retellingKeywords)
    }

    private fun toCard(cardNode: JsonNode): PostActivityConfig.Card = PostActivityConfig.Card(
        id = cardNode.path(CARD_ID_FIELD).asText(),
        text = cardNode.path(CARD_TEXT_FIELD).asText(),
        correctOrder = cardNode.path(CARD_CORRECT_ORDER_FIELD).asInt(),
        imageUrl = readImageUrl(cardNode),
    )

    private fun readImageUrl(cardNode: JsonNode): String? {
        val imageUrlNode: JsonNode = cardNode.path(CARD_IMAGE_URL_FIELD)
        if (imageUrlNode.isMissingNode || imageUrlNode.isNull) {
            return null
        }

        return imageUrlNode.asText()
    }

    private companion object {
        const val CARDS_FIELD = "cards"
        const val CARD_ID_FIELD = "id"
        const val CARD_TEXT_FIELD = "text"
        const val CARD_CORRECT_ORDER_FIELD = "correct_order"
        const val CARD_IMAGE_URL_FIELD = "image_url"
        const val RETELLING_KEYWORDS_FIELD = "retelling_keywords"
    }
}
