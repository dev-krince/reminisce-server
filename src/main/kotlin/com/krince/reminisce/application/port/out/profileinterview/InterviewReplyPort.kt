package com.krince.reminisce.application.port.out.profileinterview

import com.krince.reminisce.application.port.out.conversation.ConversationTurn
import com.krince.reminisce.domain.model.profileinterview.vo.InterviewStage

class InterviewReplyContext(
    val stage: InterviewStage,
    val stageOpening: Boolean,
    val childName: String?,
    val childUtterance: String,
    val recentTurns: List<ConversationTurn> = emptyList(),
)

interface InterviewReplyPort {
    fun generate(context: InterviewReplyContext): String
}
