package com.krince.reminisce.application.port.out.wordbook

import com.krince.reminisce.domain.model.wordbook.StoryWordGroup

interface LoadStoryWordPort {
    fun findAllGroups(): List<StoryWordGroup>
}
