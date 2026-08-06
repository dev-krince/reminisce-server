package com.krince.reminisce.infra.adapter.out.persistence.speakingsession.mapper

import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.speakingsession.SpeakingSession
import com.krince.reminisce.domain.model.speakingsession.vo.ResponseMode
import com.krince.reminisce.domain.model.speakingsession.vo.SceneEndReason
import com.krince.reminisce.domain.model.speakingsession.vo.SessionStatus
import com.krince.reminisce.domain.model.speakingsession.vo.SpeakingSessionId
import com.krince.reminisce.domain.model.story.vo.StoryId
import com.krince.reminisce.domain.model.story.vo.ThinkingElement
import com.krince.reminisce.infra.adapter.out.persistence.speakingsession.entity.SpeakingSessionOrmEntity

object SpeakingSessionMapper {
    fun toDomain(ormEntity: SpeakingSessionOrmEntity): SpeakingSession = SpeakingSession(
        sessionId = SpeakingSessionId(ormEntity.sessionId),
        childId = ChildId(ormEntity.childId),
        storyId = StoryId(ormEntity.storyId),
        status = SessionStatus.valueOf(ormEntity.status),
        currentSceneId = ormEntity.currentSceneId,
        startedAt = ormEntity.startedAt,
        lastActivityAt = ormEntity.lastActivityAt,
        createdDate = ormEntity.createdDate,
        modifiedDate = ormEntity.modifiedDate,
        accumulatedElements = ormEntity.accumulatedElements ?: emptyList(),
        currentChildTurnCount = ormEntity.currentChildTurnCount,
        turnsWithoutNewElement = ormEntity.turnsWithoutNewElement,
        consecutiveLowInformationTurns = ormEntity.consecutiveLowInformationTurns,
        sceneGoalMet = ormEntity.sceneGoalMet,
        sceneEndReason = ormEntity.sceneEndReason?.let { SceneEndReason.valueOf(it) },
        lastResponseMode = ormEntity.lastResponseMode?.let { ResponseMode.valueOf(it) },
        lastGuidanceTarget = ormEntity.lastGuidanceTarget?.let { ThinkingElement.valueOf(it) },
    )

    fun toEntity(domain: SpeakingSession): SpeakingSessionOrmEntity = SpeakingSessionOrmEntity(
        sessionId = domain.sessionId.value,
        childId = domain.childId.value,
        storyId = domain.storyId.value,
        currentSceneId = domain.currentSceneId,
        status = domain.status.name,
        startedAt = domain.startedAt,
        lastActivityAt = domain.lastActivityAt,
        accumulatedElements = domain.accumulatedElements,
        currentChildTurnCount = domain.currentChildTurnCount,
        turnsWithoutNewElement = domain.turnsWithoutNewElement,
        consecutiveLowInformationTurns = domain.consecutiveLowInformationTurns,
        sceneGoalMet = domain.sceneGoalMet,
        sceneEndReason = domain.sceneEndReason?.name,
        lastResponseMode = domain.lastResponseMode?.name,
        lastGuidanceTarget = domain.lastGuidanceTarget?.name,
    ).apply {
        createdDate = domain.createdDate
        modifiedDate = domain.modifiedDate
    }
}
