package com.krince.reminisce.infra.adapter.out.persistence.savedstory.mapper

import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.savedstory.SavedStory
import com.krince.reminisce.domain.model.savedstory.vo.SavedStoryId
import com.krince.reminisce.domain.model.story.vo.StoryId
import com.krince.reminisce.infra.adapter.out.persistence.savedstory.entity.SavedStoryOrmEntity

object SavedStoryMapper {
    fun toDomain(ormEntity: SavedStoryOrmEntity): SavedStory = SavedStory(
        savedStoryId = SavedStoryId(ormEntity.savedStoryId),
        childId = ChildId(ormEntity.childId),
        storyId = StoryId(ormEntity.storyId),
        createdDate = ormEntity.createdDate,
    )

    fun toEntity(domain: SavedStory): SavedStoryOrmEntity = SavedStoryOrmEntity(
        savedStoryId = domain.savedStoryId.value,
        childId = domain.childId.value,
        storyId = domain.storyId.value,
    ).apply {
        createdDate = domain.createdDate
    }
}
