package com.krince.reminisce.domain.model.report

data class RepresentativeUtterance(
    val messageId: String?,
    val text: String?,
    val situation: String,
    val reason: String,
    val strengths: String,
    val practiceTip: String,
    val commentary: String,
    val chips: List<String>,
)
