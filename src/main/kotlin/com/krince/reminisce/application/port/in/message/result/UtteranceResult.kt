package com.krince.reminisce.application.port.`in`.message.result

import com.krince.reminisce.domain.model.message.Message
import java.time.LocalDateTime

class UtteranceResult(
    val messageId: String,
    val sceneId: String,
    val speakerType: String,
    val turnOrder: Long,
    val text: String,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(message: Message): UtteranceResult = UtteranceResult(
            messageId = message.messageId.value,
            sceneId = message.sceneId.value,
            speakerType = message.speakerType.name,
            turnOrder = message.turnOrder,
            text = message.text,
            createdAt = message.createdAt,
        )
    }
}
