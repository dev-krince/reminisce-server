package com.krince.reminisce.application.port.`in`.admin.command

class UpdateSceneTurnsCommand(
    val adminKey: String,
    val sceneId: String,
    val preferredTurns: Int?,
    val maxTurns: Int?,
)
