package com.krince.reminisce.infra.adapter.out.persistence.storyprofile

import com.krince.reminisce.application.port.out.storyprofile.CommandStoryProfilePort
import com.krince.reminisce.application.port.out.storyprofile.LoadStoryProfilePort
import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.storyprofile.StoryProfile
import com.krince.reminisce.infra.adapter.out.persistence.storyprofile.entity.StoryProfileOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.storyprofile.mapper.StoryProfileMapper
import org.springframework.stereotype.Component

@Component
class StoryProfileOrmAdapter(
    private val repository: StoryProfileRepository,
) : LoadStoryProfilePort, CommandStoryProfilePort {

    override fun findByChild(childId: ChildId): StoryProfile? {
        val entity: StoryProfileOrmEntity = repository.findByChildId(childId.value) ?: return null

        return StoryProfileMapper.toDomain(entity)
    }

    override fun save(profile: StoryProfile): StoryProfile {
        val savedEntity: StoryProfileOrmEntity = repository.saveAndFlush(StoryProfileMapper.toEntity(profile))

        return StoryProfileMapper.toDomain(savedEntity)
    }

    override fun deleteAllByChildIds(childIds: List<ChildId>) {
        if (childIds.isEmpty()) {
            return
        }

        repository.deleteAllByChildIdIn(childIds.map { it.value })
    }
}
