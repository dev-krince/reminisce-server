package com.krince.reminisce.application.service.story

import com.krince.reminisce.application.port.access.child.ChildAccessPort
import com.krince.reminisce.application.port.access.savedstory.SavedStoryAccessPort
import com.krince.reminisce.application.port.`in`.story.command.GetStoriesCommand
import com.krince.reminisce.application.port.`in`.story.command.GetStoryCommand
import com.krince.reminisce.application.port.`in`.story.result.StoryDetailResult
import com.krince.reminisce.application.port.`in`.story.result.StorySummaryResult
import com.krince.reminisce.application.port.`in`.story.usecase.GetStoriesUseCase
import com.krince.reminisce.application.port.`in`.story.usecase.GetStoryUseCase
import com.krince.reminisce.application.port.`in`.story.result.SceneResult
import com.krince.reminisce.application.port.out.story.LoadStoryPort
import com.krince.reminisce.application.port.out.tts.NARRATOR_VOICE_PROFILE
import com.krince.reminisce.application.port.out.tts.TtsPort
import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.story.Scene
import com.krince.reminisce.domain.model.story.Story
import com.krince.reminisce.domain.model.story.vo.SceneType
import com.krince.reminisce.domain.model.story.vo.StoryId
import com.krince.reminisce.domain.model.user.vo.UserId
import com.krince.reminisce.shared.exception.NotFoundException
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND_STORY
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class StoryQueryService(
    private val loadStoryPort: LoadStoryPort,
    private val savedStoryAccessPort: SavedStoryAccessPort,
    private val childAccessPort: ChildAccessPort,
    private val ttsPort: TtsPort,
) : GetStoriesUseCase, GetStoryUseCase {

    @Transactional(readOnly = true)
    override fun execute(command: GetStoriesCommand): List<StorySummaryResult> {
        val stories: List<Story> =
            loadStoryPort.findPublished(command.genre, command.topic, command.titleKeyword, command.sort)
        val bookmarkedStoryIds: Set<String> = resolveBookmarkedStoryIds(command)

        return stories.map { StorySummaryResult.from(it, it.storyId.value in bookmarkedStoryIds) }
    }

    private fun resolveBookmarkedStoryIds(command: GetStoriesCommand): Set<String> {
        val childId: String = command.childId ?: return emptySet()
        val ownedChildId = ChildId(childId)
        verifyOwnership(ownedChildId, UserId(command.guardianId ?: return emptySet()))

        return savedStoryAccessPort.findBookmarkedStoryIds(ownedChildId)
    }

    private fun verifyOwnership(childId: ChildId, guardianId: UserId) {
        val ownerId: UserId? = childAccessPort.findGuardianId(childId)
        if (ownerId == null || ownerId != guardianId) {
            throw NotFoundException(NOT_FOUND, NOT_FOUND.message)
        }
    }

    override fun execute(command: GetStoryCommand): StoryDetailResult {
        val story: Story = loadPublishedStory(command.storyId)
        val childName: String? = resolveOwnedChildName(command)
        val scenes: List<SceneResult> = story.scenes.map { sceneResult(it, childName) }

        return StoryDetailResult.from(story, scenes)
    }

    private fun resolveOwnedChildName(command: GetStoryCommand): String? {
        val childId: String = command.childId ?: return null
        val ownedChildId = ChildId(childId)
        verifyOwnership(ownedChildId, UserId(command.guardianId ?: return null))

        return childAccessPort.findChildName(ownedChildId)
    }

    private fun loadPublishedStory(storyId: String): Story =
        loadStoryPort.findByIdWithScenesPublished(StoryId(storyId))
            ?: throw NotFoundException(NOT_FOUND_STORY, NOT_FOUND_STORY.message)

    private fun sceneResult(scene: Scene, childName: String?): SceneResult {
        val narrationAudio: String? = narrationAudio(scene)
        val missionExplanationAudio: String? = missionExplanationAudio(scene)
        if (childName == null) {
            return SceneResult.from(scene, null, null, narrationAudio, missionExplanationAudio)
        }
        val personalized: Scene = scene.personalizedFor(childName)
        val voiceProfile: String? = personalized.characterVoice?.voiceProfile
        val openingAudio: String? = personalized.characterOpening?.let { ttsPort.synthesize(it, voiceProfile) }
        val closingAudio: String? = personalized.characterClosing?.let { ttsPort.synthesize(it, voiceProfile) }

        return SceneResult.from(personalized, openingAudio, closingAudio, narrationAudio, missionExplanationAudio)
    }

    private fun narrationAudio(scene: Scene): String? {
        if (scene.sceneType != SceneType.NARRATION) {
            return null
        }

        return ttsPort.synthesize(scene.sceneDescription, NARRATOR_VOICE_PROFILE)
    }

    private fun missionExplanationAudio(scene: Scene): String? =
        scene.mission?.explanationText()?.let { ttsPort.synthesize(it, NARRATOR_VOICE_PROFILE) }
}
