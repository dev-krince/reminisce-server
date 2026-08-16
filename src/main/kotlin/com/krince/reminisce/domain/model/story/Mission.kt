package com.krince.reminisce.domain.model.story

import com.krince.reminisce.domain.model.story.vo.MissionType
import com.krince.reminisce.domain.model.story.vo.WordCard

data class Mission(
    val goal: String,
    val examples: List<String>,
    val type: MissionType = MissionType.SPEAKING,
    val wordCards: List<WordCard>? = null,
)
