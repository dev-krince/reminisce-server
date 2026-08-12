package com.krince.reminisce.infra.adapter.out.persistence.postactivityresult.mapper

import com.krince.reminisce.domain.model.postactivityresult.PostActivityResult
import com.krince.reminisce.domain.model.postactivityresult.vo.PostActivityResultId
import com.krince.reminisce.domain.model.speakingsession.vo.SpeakingSessionId
import com.krince.reminisce.infra.adapter.out.persistence.postactivityresult.entity.PostActivityResultOrmEntity

object PostActivityResultMapper {
    fun toDomain(ormEntity: PostActivityResultOrmEntity): PostActivityResult = PostActivityResult(
        id = PostActivityResultId(ormEntity.id),
        sessionId = SpeakingSessionId(ormEntity.sessionId),
        submittedOrder = ormEntity.submittedOrder ?: emptyList(),
        isOrderCorrect = ormEntity.isOrderCorrect,
        attemptCount = ormEntity.attemptCount,
        retellingText = ormEntity.retellingText,
        retellingAudioUrl = ormEntity.retellingAudioUrl,
        completedAt = ormEntity.completedAt,
        createdDate = ormEntity.createdDate,
        modifiedDate = ormEntity.modifiedDate,
    )

    fun toEntity(domain: PostActivityResult): PostActivityResultOrmEntity = PostActivityResultOrmEntity(
        id = domain.id.value,
        sessionId = domain.sessionId.value,
        submittedOrder = domain.submittedOrder,
        isOrderCorrect = domain.isOrderCorrect,
        attemptCount = domain.attemptCount,
        retellingText = domain.retellingText,
        retellingAudioUrl = domain.retellingAudioUrl,
        completedAt = domain.completedAt,
    ).apply {
        createdDate = domain.createdDate
        modifiedDate = domain.modifiedDate
    }
}
