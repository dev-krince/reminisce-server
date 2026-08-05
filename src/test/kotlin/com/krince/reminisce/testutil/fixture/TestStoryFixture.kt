package com.krince.reminisce.testutil.fixture

import com.krince.reminisce.infra.adapter.out.persistence.story.SceneRepository
import com.krince.reminisce.infra.adapter.out.persistence.story.StoryRepository
import com.krince.reminisce.infra.adapter.out.persistence.story.StoryTopicRepository
import com.krince.reminisce.infra.adapter.out.persistence.story.entity.SceneOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.story.entity.StoryOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.story.entity.StoryTopicOrmEntity
import org.springframework.stereotype.Component

@Component
class TestStoryFixture(
    private val storyRepository: StoryRepository,
    private val sceneRepository: SceneRepository,
    private val storyTopicRepository: StoryTopicRepository,
) {
    fun saveStory(entity: StoryOrmEntity): StoryOrmEntity = storyRepository.save(entity)

    fun saveScene(entity: SceneOrmEntity): SceneOrmEntity = sceneRepository.save(entity)

    fun saveTopic(entity: StoryTopicOrmEntity): StoryTopicOrmEntity = storyTopicRepository.save(entity)

    fun deleteAllBatch() {
        storyTopicRepository.deleteAllInBatch()
        sceneRepository.deleteAllInBatch()
        storyRepository.deleteAllInBatch()
    }
}
