package com.krince.reminisce.infra.adapter.out.persistence.report.converter

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.krince.reminisce.domain.model.report.HomeGuide
import com.krince.reminisce.domain.model.report.ParticipationItem
import com.krince.reminisce.domain.model.report.ReportOverall
import com.krince.reminisce.domain.model.report.ReportSpeechAnalysis
import com.krince.reminisce.domain.model.report.RepresentativeUtterance
import com.krince.reminisce.domain.model.report.SceneHighlight
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

private val objectMapper: ObjectMapper = jacksonObjectMapper()

@Converter
class ReportOverallConverter : AttributeConverter<ReportOverall?, String?> {
    override fun convertToDatabaseColumn(attribute: ReportOverall?): String? =
        attribute?.let { objectMapper.writeValueAsString(it) }

    override fun convertToEntityAttribute(dbData: String?): ReportOverall? {
        if (dbData.isNullOrBlank()) {
            return null
        }

        return objectMapper.readValue(dbData)
    }
}

@Converter
class ParticipationItemsConverter : AttributeConverter<List<ParticipationItem>?, String?> {
    override fun convertToDatabaseColumn(attribute: List<ParticipationItem>?): String? =
        attribute?.let { objectMapper.writeValueAsString(it) }

    override fun convertToEntityAttribute(dbData: String?): List<ParticipationItem>? {
        if (dbData.isNullOrBlank()) {
            return null
        }

        return objectMapper.readValue(dbData)
    }
}

@Converter
class ReportSpeechAnalysesConverter : AttributeConverter<List<ReportSpeechAnalysis>?, String?> {
    override fun convertToDatabaseColumn(attribute: List<ReportSpeechAnalysis>?): String? =
        attribute?.let { objectMapper.writeValueAsString(it) }

    override fun convertToEntityAttribute(dbData: String?): List<ReportSpeechAnalysis>? {
        if (dbData.isNullOrBlank()) {
            return null
        }

        return objectMapper.readValue(dbData)
    }
}

@Converter
class SceneHighlightsConverter : AttributeConverter<List<SceneHighlight>?, String?> {
    override fun convertToDatabaseColumn(attribute: List<SceneHighlight>?): String? =
        attribute?.let { objectMapper.writeValueAsString(it) }

    override fun convertToEntityAttribute(dbData: String?): List<SceneHighlight>? {
        if (dbData.isNullOrBlank()) {
            return null
        }

        return objectMapper.readValue(dbData)
    }
}

@Converter
class RepresentativeUtteranceConverter : AttributeConverter<RepresentativeUtterance?, String?> {
    override fun convertToDatabaseColumn(attribute: RepresentativeUtterance?): String? =
        attribute?.let { objectMapper.writeValueAsString(it) }

    override fun convertToEntityAttribute(dbData: String?): RepresentativeUtterance? {
        if (dbData.isNullOrBlank()) {
            return null
        }

        return objectMapper.readValue(dbData)
    }
}

@Converter
class HomeGuideConverter : AttributeConverter<HomeGuide?, String?> {
    override fun convertToDatabaseColumn(attribute: HomeGuide?): String? =
        attribute?.let { objectMapper.writeValueAsString(it) }

    override fun convertToEntityAttribute(dbData: String?): HomeGuide? {
        if (dbData.isNullOrBlank()) {
            return null
        }

        return objectMapper.readValue(dbData)
    }
}
