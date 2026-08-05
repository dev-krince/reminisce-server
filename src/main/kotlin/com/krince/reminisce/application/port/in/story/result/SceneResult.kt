package com.krince.reminisce.application.port.`in`.story.result

import com.krince.reminisce.domain.model.story.Scene
import com.krince.reminisce.domain.model.story.vo.SceneType
import com.krince.reminisce.domain.model.story.vo.ThinkingElement

class SceneResult(
    val sceneId: String,
    val sceneOrder: Int,
    val sceneType: SceneType,
    val sceneDescription: String,
    val characterName: String?,
    val characterDisplayName: String?,
    val characterOpening: String?,
    val characterClosing: String?,
    val conflict: String?,
    val sceneGoal: String?,
    val requiredElements: List<ThinkingElement>?,
    val preferredTurns: Int?,
    val maxTurns: Int?,
) {
    companion object {
        fun from(scene: Scene): SceneResult = SceneResult(
            sceneId = scene.sceneId.value,
            sceneOrder = scene.sceneOrder,
            sceneType = scene.sceneType,
            sceneDescription = scene.sceneDescription,
            characterName = scene.characterName,
            characterDisplayName = scene.characterDisplayName,
            characterOpening = scene.characterOpening,
            characterClosing = scene.characterClosing,
            conflict = scene.conflict,
            sceneGoal = scene.sceneGoal,
            requiredElements = scene.requiredElements,
            preferredTurns = scene.preferredTurns,
            maxTurns = scene.maxTurns,
        )
    }
}
