package com.krince.reminisce.infra.adapter.out.persistence.story.converter

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.krince.reminisce.domain.model.story.CharacterVoice
import com.krince.reminisce.domain.model.story.VoiceAgeGroup
import com.krince.reminisce.domain.model.story.VoiceGender
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class CharacterVoiceConverter : AttributeConverter<CharacterVoice?, String?> {
    private val objectMapper: ObjectMapper = ObjectMapper()

    override fun convertToDatabaseColumn(attribute: CharacterVoice?): String? {
        if (attribute == null) {
            return null
        }

        val rootNode: ObjectNode = objectMapper.createObjectNode()
        rootNode.put(GENDER_FIELD, attribute.gender.name)
        rootNode.put(AGE_GROUP_FIELD, attribute.ageGroup.name)
        rootNode.put(VOICE_PROFILE_FIELD, attribute.voiceProfile)

        return objectMapper.writeValueAsString(rootNode)
    }

    override fun convertToEntityAttribute(dbData: String?): CharacterVoice? {
        if (dbData == null) {
            return null
        }

        val rootNode: JsonNode = objectMapper.readTree(dbData)
        val gender: VoiceGender = VoiceGender.valueOf(rootNode.path(GENDER_FIELD).asText())
        val ageGroup: VoiceAgeGroup = VoiceAgeGroup.valueOf(rootNode.path(AGE_GROUP_FIELD).asText())
        val voiceProfile: String = rootNode.path(VOICE_PROFILE_FIELD).asText()

        return CharacterVoice(gender = gender, ageGroup = ageGroup, voiceProfile = voiceProfile)
    }

    private companion object {
        const val GENDER_FIELD = "gender"
        const val AGE_GROUP_FIELD = "ageGroup"
        const val VOICE_PROFILE_FIELD = "voiceProfile"
    }
}
