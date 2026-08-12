package com.krince.reminisce.application.port.`in`.story.result

import com.krince.reminisce.domain.model.story.CharacterVoice
import com.krince.reminisce.domain.model.story.Mission
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
    val characterOpeningAudio: String?,
    val characterClosingAudio: String?,
    val conflict: String?,
    val sceneGoal: String?,
    val requiredElements: List<ThinkingElement>?,
    val preferredTurns: Int?,
    val maxTurns: Int?,
    val mission: Mission?,
    val characterVoice: CharacterVoice?,
) {
    companion object {
        fun from(scene: Scene): SceneResult = from(scene, null, null)

        fun from(
            scene: Scene,
            characterOpeningAudio: String?,
            characterClosingAudio: String?,
        ): SceneResult = SceneResult(
            sceneId = scene.sceneId.value,
            sceneOrder = scene.sceneOrder,
            sceneType = scene.sceneType,
            sceneDescription = scene.sceneDescription,
            characterName = scene.characterName,
            characterDisplayName = scene.characterDisplayName,
            characterOpening = scene.characterOpening,
            characterClosing = scene.characterClosing,
            characterOpeningAudio = characterOpeningAudio,
            characterClosingAudio = characterClosingAudio,
            conflict = scene.conflict,
            sceneGoal = scene.sceneGoal,
            requiredElements = scene.requiredElements,
            preferredTurns = scene.preferredTurns,
            maxTurns = scene.maxTurns,
            mission = scene.mission,
            characterVoice = scene.characterVoice,
        )
    }
}
