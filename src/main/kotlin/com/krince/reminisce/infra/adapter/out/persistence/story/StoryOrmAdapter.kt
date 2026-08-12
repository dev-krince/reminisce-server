package com.krince.reminisce.infra.adapter.out.persistence.story

import com.krince.reminisce.application.port.out.story.LoadStoryPort
import com.krince.reminisce.domain.model.story.Story
import com.krince.reminisce.domain.model.story.vo.StoryGenre
import com.krince.reminisce.domain.model.story.vo.StoryId
import com.krince.reminisce.domain.model.story.vo.StorySort
import com.krince.reminisce.domain.model.story.vo.StoryStatus
import com.krince.reminisce.infra.adapter.out.persistence.story.dto.StoryAggregateEntity
import com.krince.reminisce.infra.adapter.out.persistence.story.entity.SceneOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.story.entity.StoryOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.story.entity.StoryTopicOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.story.mapper.StoryMapper
import org.springframework.stereotype.Component

@Component
class StoryOrmAdapter(
    private val storyRepository: StoryRepository,
    private val sceneRepository: SceneRepository,
    private val storyTopicRepository: StoryTopicRepository,
    private val storyQueryRepository: StoryQueryRepository,
) : LoadStoryPort {

    override fun findAllPublished(): List<Story> {
        val storyOrmEntities: List<StoryOrmEntity> = storyRepository.findAllByStatus(StoryStatus.PUBLISHED.name)

        return toSummaryDomains(storyOrmEntities)
    }

    override fun findAllPublishedByTopic(topic: String): List<Story> =
        findPublished(genre = null, topic = topic, titleKeyword = null, sort = StorySort.RECOMMENDED)

    override fun findPublished(
        genre: StoryGenre?,
        topic: String?,
        titleKeyword: String?,
        sort: StorySort,
    ): List<Story> {
        val storyOrmEntities: List<StoryOrmEntity> =
            storyQueryRepository.findPublished(genre, topic, titleKeyword, sort)

        return toSummaryDomains(storyOrmEntities)
    }

    override fun findByIdWithScenesPublished(storyId: StoryId): Story? {
        val storyOrmEntity: StoryOrmEntity =
            storyRepository.findByStoryIdAndStatus(storyId.value, StoryStatus.PUBLISHED.name) ?: return null
        val sceneOrmEntities: List<SceneOrmEntity> =
            sceneRepository.findAllByStoryIdOrderBySceneOrderAsc(storyOrmEntity.storyId)
        val storyTopicOrmEntities: List<StoryTopicOrmEntity> =
            storyTopicRepository.findAllByStoryId(storyOrmEntity.storyId)

        return StoryMapper.toDomain(
            StoryAggregateEntity(
                storyOrmEntity = storyOrmEntity,
                sceneOrmEntities = sceneOrmEntities,
                storyTopicOrmEntities = storyTopicOrmEntities,
            )
        )
    }

    private fun toSummaryDomains(storyOrmEntities: List<StoryOrmEntity>): List<Story> {
        if (storyOrmEntities.isEmpty()) {
            return emptyList()
        }

        val storyIds: List<String> = storyOrmEntities.map { it.storyId }
        val storyTopicsByStoryId: Map<String, List<StoryTopicOrmEntity>> =
            storyTopicRepository.findAllByStoryIdIn(storyIds).groupBy { it.storyId }

        return storyOrmEntities.map { storyOrmEntity ->
            StoryMapper.toDomain(
                StoryAggregateEntity(
                    storyOrmEntity = storyOrmEntity,
                    sceneOrmEntities = emptyList(),
                    storyTopicOrmEntities = storyTopicsByStoryId[storyOrmEntity.storyId].orEmpty(),
                )
            )
        }
    }
}
