package com.krince.reminisce.domain.model.report

data class CompetencyItem(
    val label: String,
    val feature: String,
    val evidenceUtterance: String?,
    val strength: String,
    val improvement: String,
)
