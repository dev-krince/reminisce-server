package com.krince.reminisce.domain.model.message

import com.krince.reminisce.domain.model.message.vo.MessageId
import com.krince.reminisce.domain.model.message.vo.SpeakerType
import com.krince.reminisce.domain.model.speakingsession.vo.SpeakingSessionId
import com.krince.reminisce.domain.model.story.vo.SceneId
import com.krince.reminisce.shared.util.UuidGenerator
import java.time.LocalDateTime

class Message(
    val messageId: MessageId,
    val sessionId: SpeakingSessionId,
    val sceneId: SceneId,
    val speakerType: SpeakerType,
    val turnOrder: Long,
    val text: String,
    val sttRawText: String?,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun childUtterance(
            sessionId: SpeakingSessionId,
            sceneId: SceneId,
            turnOrder: Long,
            text: String,
            sttRawText: String?,
            at: LocalDateTime,
        ): Message = Message(
            messageId = MessageId(UuidGenerator.generate()),
            sessionId = sessionId,
            sceneId = sceneId,
            speakerType = SpeakerType.CHILD,
            turnOrder = turnOrder,
            text = text,
            sttRawText = sttRawText,
            createdAt = at,
        )

        fun characterReply(
            sessionId: SpeakingSessionId,
            sceneId: SceneId,
            turnOrder: Long,
            text: String,
            at: LocalDateTime,
        ): Message = Message(
            messageId = MessageId(UuidGenerator.generate()),
            sessionId = sessionId,
            sceneId = sceneId,
            speakerType = SpeakerType.CHARACTER,
            turnOrder = turnOrder,
            text = text,
            sttRawText = null,
            createdAt = at,
        )
    }
}
