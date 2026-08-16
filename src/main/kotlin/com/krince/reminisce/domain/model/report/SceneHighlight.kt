package com.krince.reminisce.domain.model.report

data class SceneHighlight(
    val sceneId: String,
    val messageId: String,
    val featureSentence: String,
    val featureChips: List<String>,
)
