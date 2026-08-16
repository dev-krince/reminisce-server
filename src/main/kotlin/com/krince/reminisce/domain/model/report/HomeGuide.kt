package com.krince.reminisce.domain.model.report

data class GuideDirection(
    val headline: String,
    val description: String,
)

data class GuideQuestion(
    val label: String,
    val question: String,
    val helper: String,
)

data class HomeGuide(
    val direction: GuideDirection,
    val storyQuestions: List<GuideQuestion>,
    val dailyQuestions: List<GuideQuestion>,
    val guardianTip: String,
)
