package com.krince.reminisce.domain.model.story

import com.krince.reminisce.domain.model.story.vo.MissionType
import com.krince.reminisce.domain.model.story.vo.WordCard

data class Mission(
    val goal: String,
    val examples: List<String>,
    val type: MissionType = MissionType.SPEAKING,
    val wordCards: List<WordCard>? = null,
) {
    init {
        if (type == MissionType.WORD_ORDER) {
            require(!wordCards.isNullOrEmpty()) { "WORD_ORDER 미션은 비어있지 않은 wordCards가 필요합니다" }
        }
    }
}
