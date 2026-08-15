package com.krince.reminisce.infra.adapter.out.persistence.savedstory

import com.krince.reminisce.application.port.out.savedstory.CommandSavedStoryPort
import com.krince.reminisce.application.port.out.savedstory.LoadSavedStoryPort
import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.savedstory.SavedStory
import com.krince.reminisce.domain.model.story.vo.StoryId
import com.krince.reminisce.infra.adapter.out.persistence.savedstory.entity.SavedStoryOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.savedstory.mapper.SavedStoryMapper
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Component

@Component
class SavedStoryOrmAdapter(
    private val repository: SavedStoryRepository,
    private val savedStoryInserter: SavedStoryInserter,
) : LoadSavedStoryPort, CommandSavedStoryPort {

    override fun saveIfAbsent(savedStory: SavedStory): SavedStory {
        val ormEntity: SavedStoryOrmEntity = SavedStoryMapper.toEntity(savedStory)

        return runCatching { SavedStoryMapper.toDomain(savedStoryInserter.insert(ormEntity)) }
            .recover { cause -> recoverExistingBookmark(savedStory, cause) }
            .getOrThrow()
    }

    override fun findAllByChildId(childId: ChildId): List<SavedStory> =
        repository.findAllByChildIdOrderByCreatedDateDesc(childId.value).map { SavedStoryMapper.toDomain(it) }

    override fun findByChildIdAndStoryId(childId: ChildId, storyId: StoryId): SavedStory? =
        repository.findByChildIdAndStoryId(childId.value, storyId.value)?.let { SavedStoryMapper.toDomain(it) }

    override fun deleteByChildIdAndStoryId(childId: ChildId, storyId: StoryId) {
        repository.deleteByChildIdAndStoryId(childId.value, storyId.value)
    }

    override fun deleteAllByChildIds(childIds: List<ChildId>) {
        if (childIds.isEmpty()) {
            return
        }

        repository.deleteAllByChildIdIn(childIds.map { it.value })
    }

    private fun recoverExistingBookmark(savedStory: SavedStory, cause: Throwable): SavedStory {
        if (cause !is DataIntegrityViolationException) {
            throw cause
        }

        val existing: SavedStoryOrmEntity = repository.findByChildIdAndStoryId(
            savedStory.childId.value,
            savedStory.storyId.value,
        ) ?: throw cause

        return SavedStoryMapper.toDomain(existing)
    }
}
