package com.krince.reminisce.infra.adapter.out.persistence.message.mapper

import com.krince.reminisce.domain.model.message.Message
import com.krince.reminisce.domain.model.message.vo.MessageId
import com.krince.reminisce.domain.model.message.vo.SpeakerType
import com.krince.reminisce.domain.model.speakingsession.vo.SpeakingSessionId
import com.krince.reminisce.domain.model.story.vo.SceneId
import com.krince.reminisce.infra.adapter.out.persistence.message.entity.MessageOrmEntity

object MessageMapper {
    fun toDomain(ormEntity: MessageOrmEntity): Message = Message(
        messageId = MessageId(ormEntity.id),
        sessionId = SpeakingSessionId(ormEntity.sessionId),
        sceneId = SceneId(ormEntity.sceneId),
        speakerType = SpeakerType.valueOf(ormEntity.speakerType),
        turnOrder = ormEntity.turnOrder,
        text = ormEntity.text,
        sttRawText = ormEntity.sttRawText,
        createdAt = ormEntity.createdAt,
    )

    fun toEntity(domain: Message): MessageOrmEntity = MessageOrmEntity(
        id = domain.messageId.value,
        sessionId = domain.sessionId.value,
        sceneId = domain.sceneId.value,
        speakerType = domain.speakerType.name,
        turnOrder = domain.turnOrder,
        text = domain.text,
        sttRawText = domain.sttRawText,
        createdAt = domain.createdAt,
    )
}
