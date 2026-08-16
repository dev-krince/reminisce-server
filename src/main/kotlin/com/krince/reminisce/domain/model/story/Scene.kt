package com.krince.reminisce.domain.model.story

import com.krince.reminisce.domain.model.story.vo.SceneId
import com.krince.reminisce.domain.model.story.vo.SceneType
import com.krince.reminisce.domain.model.story.vo.StoryId
import com.krince.reminisce.domain.model.story.vo.ThinkingElement

class Scene(
    val sceneId: SceneId,
    val storyId: StoryId,
    val sceneOrder: Int,
    val chapter: Int = FIRST_CHAPTER,
    val sceneType: SceneType,
    val sceneDescription: String,
    val title: String? = null,
    val characterName: String? = null,
    val characterDisplayName: String? = null,
    val characterOpening: String? = null,
    val characterClosing: String? = null,
    val conflict: String? = null,
    val sceneGoal: String? = null,
    val requiredElements: List<ThinkingElement>? = null,
    val preferredTurns: Int? = null,
    val maxTurns: Int? = null,
    val mission: Mission? = null,
    val characterVoice: CharacterVoice? = null,
    val imageUrl: String? = null,
    val characterImageUrl: String? = null,
) {
    init {
        when (sceneType) {
            SceneType.NARRATION -> requireNoDialogueFields()
            SceneType.CHARACTER_LINE -> requireCharacterLineFields()
            SceneType.DIALOGUE -> requireDialogueFields()
        }
    }

    fun personalizedFor(childName: String?): Scene {
        if (characterOpening == null && characterClosing == null) {
            return this
        }

        return Scene(
            sceneId = sceneId,
            storyId = storyId,
            sceneOrder = sceneOrder,
            chapter = chapter,
            sceneType = sceneType,
            sceneDescription = sceneDescription,
            title = title,
            characterName = characterName,
            characterDisplayName = characterDisplayName,
            characterOpening = characterOpening?.let { ChildNamePersonalizer.personalize(it, childName) },
            characterClosing = characterClosing?.let { ChildNamePersonalizer.personalize(it, childName) },
            conflict = conflict,
            sceneGoal = sceneGoal,
            requiredElements = requiredElements,
            preferredTurns = preferredTurns,
            maxTurns = maxTurns,
            mission = mission,
            characterVoice = characterVoice,
            imageUrl = imageUrl,
            characterImageUrl = characterImageUrl,
        )
    }

    private fun requireNoDialogueFields() {
        val dialogueOnlyFields: List<Any?> = listOf(
            characterName,
            characterDisplayName,
            characterOpening,
            characterClosing,
            conflict,
            sceneGoal,
            requiredElements,
            preferredTurns,
            maxTurns,
            mission,
            characterVoice,
        )

        require(dialogueOnlyFields.all { it == null }) { "NARRATION 장면은 대화 전용 필드를 가질 수 없습니다" }
    }

    private fun requireCharacterLineFields() {
        require(characterName != null) { "CHARACTER_LINE 장면은 characterName이 필요합니다" }
        require(characterDisplayName != null) { "CHARACTER_LINE 장면은 characterDisplayName이 필요합니다" }
        require(characterOpening != null) { "CHARACTER_LINE 장면은 characterOpening이 필요합니다" }
        require(characterVoice != null) { "CHARACTER_LINE 장면은 characterVoice가 필요합니다" }
        require(sceneGoal == null) { "CHARACTER_LINE 장면은 sceneGoal을 가질 수 없습니다" }
        require(requiredElements == null) { "CHARACTER_LINE 장면은 requiredElements를 가질 수 없습니다" }
        require(maxTurns == null) { "CHARACTER_LINE 장면은 maxTurns를 가질 수 없습니다" }
        require(mission == null) { "CHARACTER_LINE 장면은 mission을 가질 수 없습니다" }
    }

    private fun requireDialogueFields() {
        require(characterName != null) { "DIALOGUE 장면은 characterName이 필요합니다" }
        require(characterDisplayName != null) { "DIALOGUE 장면은 characterDisplayName이 필요합니다" }
        require(characterVoice != null) { "DIALOGUE 장면은 characterVoice가 필요합니다" }
        require(characterImageUrl != null) { "DIALOGUE 장면은 characterImageUrl이 필요합니다" }
        require(sceneGoal != null) { "DIALOGUE 장면은 sceneGoal이 필요합니다" }
        require(!requiredElements.isNullOrEmpty()) { "DIALOGUE 장면은 비어있지 않은 requiredElements가 필요합니다" }
        require(maxTurns != null) { "DIALOGUE 장면은 maxTurns가 필요합니다" }
    }

    companion object {
        const val FIRST_CHAPTER: Int = 1
    }
}
