package com.krince.reminisce.infra.adapter.out.persistence.story.dto

import com.krince.reminisce.infra.adapter.out.persistence.story.entity.SceneOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.story.entity.StoryOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.story.entity.StoryTopicOrmEntity

class StoryAggregateEntity(
    val storyOrmEntity: StoryOrmEntity,
    val sceneOrmEntities: List<SceneOrmEntity>,
    val storyTopicOrmEntities: List<StoryTopicOrmEntity>,
)
