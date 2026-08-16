package com.krince.reminisce.infra.adapter.out.persistence.storyprofile.converter

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.krince.reminisce.domain.model.storyprofile.InterestTopic
import com.krince.reminisce.domain.model.storyprofile.ProfileFinding
import com.krince.reminisce.domain.model.storyprofile.SpeechAreaAnalysis
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

private val objectMapper: ObjectMapper = jacksonObjectMapper()

@Converter
class InterestTopicsConverter : AttributeConverter<List<InterestTopic>, String> {
    override fun convertToDatabaseColumn(attribute: List<InterestTopic>?): String =
        objectMapper.writeValueAsString(attribute ?: emptyList<InterestTopic>())

    override fun convertToEntityAttribute(dbData: String?): List<InterestTopic> {
        if (dbData.isNullOrBlank()) {
            return emptyList()
        }

        return objectMapper.readValue(dbData)
    }
}

@Converter
class ProfileFindingsConverter : AttributeConverter<List<ProfileFinding>, String> {
    override fun convertToDatabaseColumn(attribute: List<ProfileFinding>?): String =
        objectMapper.writeValueAsString(attribute ?: emptyList<ProfileFinding>())

    override fun convertToEntityAttribute(dbData: String?): List<ProfileFinding> {
        if (dbData.isNullOrBlank()) {
            return emptyList()
        }

        return objectMapper.readValue(dbData)
    }
}

@Converter
class SpeechAreaAnalysesConverter : AttributeConverter<List<SpeechAreaAnalysis>, String> {
    override fun convertToDatabaseColumn(attribute: List<SpeechAreaAnalysis>?): String =
        objectMapper.writeValueAsString(attribute ?: emptyList<SpeechAreaAnalysis>())

    override fun convertToEntityAttribute(dbData: String?): List<SpeechAreaAnalysis> {
        if (dbData.isNullOrBlank()) {
            return emptyList()
        }

        return objectMapper.readValue(dbData)
    }
}
