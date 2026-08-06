package com.krince.reminisce.application.port.out.reply

import com.krince.reminisce.domain.model.speakingsession.vo.ResponseMode
import com.krince.reminisce.domain.model.story.vo.ThinkingElement

class CharacterReplyContext(
    val characterDisplayName: String,
    val mode: ResponseMode,
    val childUtterance: String,
    val guidanceTarget: ThinkingElement?,
)

interface CharacterReplyPort {
    fun generate(context: CharacterReplyContext): String
}
