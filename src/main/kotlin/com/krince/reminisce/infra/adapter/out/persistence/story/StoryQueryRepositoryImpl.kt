package com.krince.reminisce.infra.adapter.out.persistence.story

import com.krince.reminisce.domain.model.story.vo.StoryGenre
import com.krince.reminisce.domain.model.story.vo.StorySort
import com.krince.reminisce.domain.model.story.vo.StoryStatus
import com.krince.reminisce.infra.adapter.out.persistence.story.entity.QStoryOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.story.entity.QStoryTopicOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.story.entity.StoryOrmEntity
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.JPAExpressions
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository

@Repository
class StoryQueryRepositoryImpl(private val queryFactory: JPAQueryFactory) : StoryQueryRepository {

    private val story = QStoryOrmEntity.storyOrmEntity
    private val storyTopic = QStoryTopicOrmEntity.storyTopicOrmEntity

    override fun findPublished(
        genre: StoryGenre?,
        topic: String?,
        titleKeyword: String?,
        sort: StorySort,
    ): List<StoryOrmEntity> = queryFactory
        .selectFrom(story)
        .where(
            story.status.eq(StoryStatus.PUBLISHED.name),
            genreEq(genre),
            titleContains(titleKeyword),
            topicIn(topic),
        )
        .orderBy(orderBy(sort))
        .fetch()

    private fun genreEq(genre: StoryGenre?): BooleanExpression? = genre?.let { story.storyGenre.eq(it.name) }

    private fun titleContains(titleKeyword: String?): BooleanExpression? {
        if (titleKeyword.isNullOrBlank()) {
            return null
        }

        return story.title.containsIgnoreCase(titleKeyword)
    }

    private fun topicIn(topic: String?): BooleanExpression? {
        if (topic == null) {
            return null
        }

        return story.storyId.`in`(
            JPAExpressions
                .select(storyTopic.storyId)
                .from(storyTopic)
                .where(storyTopic.topic.eq(topic)),
        )
    }

    private fun orderBy(sort: StorySort): OrderSpecifier<*> = when (sort) {
        StorySort.RECOMMENDED -> story.createdDate.asc()
        StorySort.DIFFICULTY -> story.difficulty.asc()
        StorySort.LATEST -> story.createdDate.desc()
    }
}
