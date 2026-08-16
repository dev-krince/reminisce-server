package com.krince.reminisce.infra.adapter.out.persistence.story.mapper

import com.krince.reminisce.domain.model.story.Scene
import com.krince.reminisce.domain.model.story.Story
import com.krince.reminisce.domain.model.story.vo.Difficulty
import com.krince.reminisce.domain.model.story.vo.SceneId
import com.krince.reminisce.domain.model.story.vo.SceneType
import com.krince.reminisce.domain.model.story.vo.StoryGenre
import com.krince.reminisce.domain.model.story.vo.StoryId
import com.krince.reminisce.domain.model.story.vo.StoryStatus
import com.krince.reminisce.infra.adapter.out.persistence.story.dto.StoryAggregateEntity
import com.krince.reminisce.infra.adapter.out.persistence.story.entity.SceneOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.story.entity.StoryOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.story.entity.StoryTopicOrmEntity
import com.krince.reminisce.shared.util.UuidGenerator

object StoryMapper {
    fun toDomain(aggregateEntity: StoryAggregateEntity): Story {
        val storyOrmEntity: StoryOrmEntity = aggregateEntity.storyOrmEntity

        return Story(
            storyId = StoryId(storyOrmEntity.storyId),
            title = storyOrmEntity.title,
            summary = storyOrmEntity.summary,
            intro = storyOrmEntity.intro,
            situation = storyOrmEntity.situation,
            childRole = storyOrmEntity.childRole,
            difficulty = Difficulty(storyOrmEntity.difficulty),
            estimatedMinutes = storyOrmEntity.estimatedMinutes?.toInt(),
            representativeImageUrl = storyOrmEntity.representativeImageUrl,
            status = StoryStatus.valueOf(storyOrmEntity.status),
            postActivityConfig = storyOrmEntity.postActivityConfig,
            topics = aggregateEntity.storyTopicOrmEntities.map { it.topic },
            genre = storyOrmEntity.storyGenre?.let { StoryGenre.valueOf(it) },
            scenes = aggregateEntity.sceneOrmEntities.map { toScene(it) },
            createdDate = storyOrmEntity.createdDate,
            modifiedDate = storyOrmEntity.modifiedDate,
        )
    }

    fun toEntity(story: Story): StoryAggregateEntity = StoryAggregateEntity(
        storyOrmEntity = toStoryOrmEntity(story),
        sceneOrmEntities = story.scenes.map { toSceneOrmEntity(it) },
        storyTopicOrmEntities = story.topics.map { toStoryTopicOrmEntity(story.storyId, it) },
    )

    private fun toScene(ormEntity: SceneOrmEntity): Scene = Scene(
        sceneId = SceneId(ormEntity.sceneId),
        storyId = StoryId(ormEntity.storyId),
        sceneOrder = ormEntity.sceneOrder.toInt(),
        chapter = ormEntity.chapter.toInt(),
        sceneType = SceneType.valueOf(ormEntity.sceneType),
        sceneDescription = ormEntity.sceneDescription,
        title = ormEntity.title,
        characterName = ormEntity.characterName,
        characterDisplayName = ormEntity.characterDisplayName,
        characterOpening = ormEntity.characterOpening,
        characterClosing = ormEntity.characterClosing,
        conflict = ormEntity.conflict,
        sceneGoal = ormEntity.sceneGoal,
        requiredElements = ormEntity.requiredElements,
        preferredTurns = ormEntity.preferredTurns?.toInt(),
        maxTurns = ormEntity.maxTurns?.toInt(),
        mission = ormEntity.mission,
        characterVoice = ormEntity.characterVoice,
        imageUrl = ormEntity.imageUrl,
        characterImageUrl = ormEntity.characterImageUrl,
    )

    private fun toStoryOrmEntity(story: Story): StoryOrmEntity = StoryOrmEntity(
        storyId = story.storyId.value,
        title = story.title,
        summary = story.summary,
        intro = story.intro,
        situation = story.situation,
        childRole = story.childRole,
        difficulty = story.difficulty.value,
        estimatedMinutes = story.estimatedMinutes?.toShort(),
        representativeImageUrl = story.representativeImageUrl,
        status = story.status.name,
        storyGenre = story.genre?.name,
        postActivityConfig = story.postActivityConfig,
    ).apply {
        createdDate = story.createdDate
        modifiedDate = story.modifiedDate
    }

    private fun toSceneOrmEntity(scene: Scene): SceneOrmEntity = SceneOrmEntity(
        sceneId = scene.sceneId.value,
        storyId = scene.storyId.value,
        sceneOrder = scene.sceneOrder.toShort(),
        chapter = scene.chapter.toShort(),
        sceneType = scene.sceneType.name,
        sceneDescription = scene.sceneDescription,
        title = scene.title,
        characterName = scene.characterName,
        characterDisplayName = scene.characterDisplayName,
        characterOpening = scene.characterOpening,
        characterClosing = scene.characterClosing,
        conflict = scene.conflict,
        sceneGoal = scene.sceneGoal,
        requiredElements = scene.requiredElements,
        preferredTurns = scene.preferredTurns?.toShort(),
        maxTurns = scene.maxTurns?.toShort(),
        mission = scene.mission,
        characterVoice = scene.characterVoice,
        imageUrl = scene.imageUrl,
        characterImageUrl = scene.characterImageUrl,
    )

    private fun toStoryTopicOrmEntity(storyId: StoryId, topic: String): StoryTopicOrmEntity = StoryTopicOrmEntity(
        id = UuidGenerator.generate(),
        storyId = storyId.value,
        topic = topic,
    )
}
