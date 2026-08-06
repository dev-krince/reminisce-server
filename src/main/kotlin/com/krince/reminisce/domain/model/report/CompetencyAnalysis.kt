package com.krince.reminisce.domain.model.report

data class CompetencyAnalysis(
    val vocabulary: CompetencyItem,
    val perspectiveEmpathy: CompetencyItem,
    val emotion: CompetencyItem,
    val interaction: CompetencyItem,
    val thoughtReason: CompetencyItem,
    val resultSolution: CompetencyItem,
)
