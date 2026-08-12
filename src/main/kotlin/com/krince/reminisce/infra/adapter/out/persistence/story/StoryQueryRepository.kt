package com.krince.reminisce.infra.adapter.out.persistence.story

import com.krince.reminisce.domain.model.story.vo.StoryGenre
import com.krince.reminisce.domain.model.story.vo.StorySort
import com.krince.reminisce.infra.adapter.out.persistence.story.entity.StoryOrmEntity

interface StoryQueryRepository {
    fun findPublished(genre: StoryGenre?, topic: String?, titleKeyword: String?, sort: StorySort): List<StoryOrmEntity>
}
