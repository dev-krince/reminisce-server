package com.krince.reminisce.application.facade.story

import com.krince.reminisce.application.port.access.story.ResumableStoryDisplayInfo
import com.krince.reminisce.application.port.access.story.StoryAccessPort
import com.krince.reminisce.application.port.access.story.StoryReportScene
import com.krince.reminisce.application.port.access.story.StoryReportSnapshot
import com.krince.reminisce.application.port.out.story.LoadStoryPort
import com.krince.reminisce.domain.model.story.Scene
import com.krince.reminisce.domain.model.story.Story
import com.krince.reminisce.domain.model.story.vo.PostActivityConfig
import com.krince.reminisce.domain.model.story.vo.SceneType
import com.krince.reminisce.domain.model.story.vo.StoryId
import org.springframework.stereotype.Service

@Service
class StoryAccessFacade(
    private val loadStoryPort: LoadStoryPort,
) : StoryAccessPort {

    override fun existsPublished(storyId: StoryId): Boolean =
        loadStoryPort.findByIdWithScenesPublished(storyId) != null

    override fun findIntro(storyId: StoryId): String? {
        val story: Story = loadStoryPort.findByIdWithScenesPublished(storyId) ?: return null

        return story.intro
    }

    override fun findFirstSceneId(storyId: StoryId): String? {
        val story: Story = loadStoryPort.findByIdWithScenesPublished(storyId) ?: return null

        return story.scenes.firstOrNull()?.sceneId?.value
    }

    override fun findScene(storyId: StoryId, sceneId: String): Scene? {
        val story: Story = loadStoryPort.findByIdWithScenesPublished(storyId) ?: return null

        return story.scenes.firstOrNull { it.sceneId.value == sceneId }
    }

    override fun findNextScene(storyId: StoryId, currentSceneId: String): Scene? {
        val story: Story = loadStoryPort.findByIdWithScenesPublished(storyId) ?: return null
        val orderedScenes: List<Scene> = story.scenes.sortedBy { it.sceneOrder }
        val currentScene: Scene = orderedScenes.firstOrNull { it.sceneId.value == currentSceneId } ?: return null

        return orderedScenes.firstOrNull { it.sceneOrder > currentScene.sceneOrder }
    }

    override fun findPreviousChapterFirstScene(storyId: StoryId, currentSceneId: String): Scene? {
        val story: Story = loadStoryPort.findByIdWithScenesPublished(storyId) ?: return null
        val currentScene: Scene = story.scenes.firstOrNull { it.sceneId.value == currentSceneId } ?: return null

        return firstSceneOfChapter(story, currentScene.chapter - 1)
    }

    private fun firstSceneOfChapter(story: Story, chapter: Int): Scene? = story.scenes
        .filter { it.chapter == chapter }
        .minByOrNull { it.sceneOrder }

    override fun findPrecedingCharacterLine(storyId: StoryId, currentSceneId: String): Scene? {
        val story: Story = loadStoryPort.findByIdWithScenesPublished(storyId) ?: return null
        val orderedScenes: List<Scene> = story.scenes.sortedBy { it.sceneOrder }
        val currentScene: Scene = orderedScenes.firstOrNull { it.sceneId.value == currentSceneId } ?: return null

        return orderedScenes
            .filter { it.sceneOrder < currentScene.sceneOrder }
            .lastOrNull { it.sceneType == SceneType.CHARACTER_LINE && it.characterName == currentScene.characterName }
    }

    override fun findPostActivityConfig(storyId: StoryId): PostActivityConfig? {
        val story: Story = loadStoryPort.findByIdWithScenesPublished(storyId) ?: return null

        return story.postActivityConfig
    }

    override fun findResumableDisplayInfo(storyId: StoryId, currentSceneId: String?): ResumableStoryDisplayInfo? {
        val story: Story = loadStoryPort.findByIdWithScenesPublished(storyId) ?: return null

        return ResumableStoryDisplayInfo(
            title = story.title,
            representativeImageUrl = story.representativeImageUrl,
            difficulty = story.difficulty.value,
            topics = story.topics,
            currentChapter = currentChapterOf(story, currentSceneId),
            totalChapters = story.scenes.maxOfOrNull { it.chapter } ?: NO_CHAPTER,
        )
    }

    override fun findReportSnapshot(storyId: StoryId): StoryReportSnapshot? {
        val story: Story = loadStoryPort.findByIdWithScenesPublished(storyId) ?: return null

        return StoryReportSnapshot(
            title = story.title,
            scenes = story.scenes.map { scene ->
                StoryReportScene(
                    sceneId = scene.sceneId.value,
                    sceneOrder = scene.sceneOrder,
                    sceneType = scene.sceneType,
                    description = scene.sceneDescription,
                    goal = scene.sceneGoal,
                    sceneTitle = scene.title,
                    imageUrl = scene.imageUrl,
                    characterDisplayName = scene.characterDisplayName,
                )
            },
        )
    }

    private fun currentChapterOf(story: Story, currentSceneId: String?): Int {
        if (currentSceneId == null) {
            return NO_CHAPTER
        }

        return story.scenes.firstOrNull { it.sceneId.value == currentSceneId }?.chapter ?: NO_CHAPTER
    }

    companion object {
        private const val NO_CHAPTER: Int = 0
    }
}
