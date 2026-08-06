package com.krince.reminisce.application.port.`in`.postactivity.result

data class CardOrderResult(
    val isOrderCorrect: Boolean,
    val attemptCount: Int,
    val retellingKeywords: List<String>,
)
