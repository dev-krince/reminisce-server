package com.krince.reminisce.domain.model.report

data class ReportSpeechAnalysis(
    val area: String,
    val summary: String,
    val keywords: List<String>,
    val feature: String,
    val evidenceUtterance: String?,
    val strength: String,
    val improvement: String,
)
