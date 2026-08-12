package com.krince.reminisce.application.port.`in`.story.command

import com.krince.reminisce.domain.model.story.vo.StoryGenre
import com.krince.reminisce.domain.model.story.vo.StorySort

class GetStoriesCommand(
    val topic: String?,
    val genre: StoryGenre?,
    val titleKeyword: String?,
    val sort: StorySort,
)
