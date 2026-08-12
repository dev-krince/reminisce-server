package com.krince.reminisce.application.port.`in`.wordbook.command

class SaveWordCommand(
    val childId: String,
    val guardianId: String,
    val word: String,
    val meaning: String?,
    val sourceSceneId: String?,
)
