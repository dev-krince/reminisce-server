package com.krince.reminisce.testutil.fixture

import com.krince.reminisce.infra.adapter.out.persistence.storyprofile.StoryProfileRepository
import com.krince.reminisce.infra.adapter.out.persistence.storyprofile.entity.StoryProfileOrmEntity
import org.springframework.stereotype.Component

@Component
class TestStoryProfileFixture(
    private val storyProfileRepository: StoryProfileRepository,
) {
    fun findByChildId(childId: String): StoryProfileOrmEntity? = storyProfileRepository.findByChildId(childId)

    fun count(): Long = storyProfileRepository.count()

    fun deleteAllBatch() {
        storyProfileRepository.deleteAllInBatch()
    }
}
