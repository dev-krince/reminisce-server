package com.krince.reminisce.application.port.out.reply

import com.krince.reminisce.application.port.out.conversation.ConversationTurn
import com.krince.reminisce.domain.model.speakingsession.vo.ResponseMode
import com.krince.reminisce.domain.model.story.vo.ThinkingElement

class CharacterReplyContext(
    val characterDisplayName: String,
    val mode: ResponseMode,
    val childUtterance: String,
    val guidanceTarget: ThinkingElement?,
    val characterOpening: String? = null,
    val conflict: String? = null,
    val sceneGoal: String? = null,
    val childName: String? = null,
    val recentTurns: List<ConversationTurn> = emptyList(),
)

interface CharacterReplyPort {
    fun generate(context: CharacterReplyContext): String
}
