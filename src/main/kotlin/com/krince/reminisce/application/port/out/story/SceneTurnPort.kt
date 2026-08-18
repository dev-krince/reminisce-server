package com.krince.reminisce.application.port.out.story

class SceneTurns(
    val preferredTurns: Int?,
    val maxTurns: Int?,
)

interface SceneTurnPort {
    fun findTurns(sceneId: String): SceneTurns?

    fun updateTurns(sceneId: String, preferredTurns: Int?, maxTurns: Int?)
}
