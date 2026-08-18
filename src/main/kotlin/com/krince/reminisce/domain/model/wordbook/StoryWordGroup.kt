package com.krince.reminisce.domain.model.wordbook

import com.krince.reminisce.domain.model.story.vo.StoryId

class StoryWordGroup(
    val storyId: StoryId,
    val storyTitle: String,
    val words: List<StoryWord>,
)
