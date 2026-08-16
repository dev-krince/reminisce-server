package com.krince.reminisce.application.port.out.mission

data class MissionJudgeContext(
    val goal: String,
    val examples: List<String>,
    val text: String,
)
