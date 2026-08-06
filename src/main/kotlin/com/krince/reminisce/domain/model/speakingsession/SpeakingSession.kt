package com.krince.reminisce.domain.model.speakingsession

import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.speakingsession.vo.ResponseMode
import com.krince.reminisce.domain.model.speakingsession.vo.SceneEndReason
import com.krince.reminisce.domain.model.speakingsession.vo.SessionStatus
import com.krince.reminisce.domain.model.speakingsession.vo.SpeakingSessionId
import com.krince.reminisce.domain.model.story.vo.StoryId
import com.krince.reminisce.domain.model.story.vo.ThinkingElement
import com.krince.reminisce.domain.model.utteranceanalysis.vo.UtteranceValidity
import com.krince.reminisce.shared.util.UuidGenerator
import java.time.LocalDateTime

data class SpeakingSession(
    val sessionId: SpeakingSessionId,
    val childId: ChildId,
    val storyId: StoryId,
    val status: SessionStatus,
    val currentSceneId: String? = null,
    val startedAt: LocalDateTime,
    val lastActivityAt: LocalDateTime,
    val createdDate: LocalDateTime? = null,
    val modifiedDate: LocalDateTime? = null,
    val accumulatedElements: List<ThinkingElement> = emptyList(),
    val currentChildTurnCount: Int = 0,
    val turnsWithoutNewElement: Int = 0,
    val consecutiveLowInformationTurns: Int = 0,
    val sceneGoalMet: Boolean = false,
    val sceneEndReason: SceneEndReason? = null,
    val lastResponseMode: ResponseMode? = null,
    val lastGuidanceTarget: ThinkingElement? = null,
) {
    fun transitionToScene(nextSceneId: String, at: LocalDateTime): SpeakingSession =
        copy(
            currentSceneId = nextSceneId,
            accumulatedElements = emptyList(),
            currentChildTurnCount = 0,
            turnsWithoutNewElement = 0,
            consecutiveLowInformationTurns = 0,
            sceneGoalMet = false,
            sceneEndReason = null,
            lastResponseMode = null,
            lastGuidanceTarget = null,
            lastActivityAt = at,
        )

    fun enterPostActivity(at: LocalDateTime): SpeakingSession =
        copy(status = SessionStatus.POST_ACTIVITY, lastActivityAt = at)

    fun complete(at: LocalDateTime): SpeakingSession =
        copy(status = SessionStatus.COMPLETED, lastActivityAt = at)

    fun accumulate(newTypes: List<ThinkingElement>, at: LocalDateTime): SpeakingSession =
        copy(accumulatedElements = (accumulatedElements + newTypes).distinct(), lastActivityAt = at)

    fun advanceTurn(
        hasNewElement: Boolean,
        validity: UtteranceValidity,
        missingElements: List<ThinkingElement>,
        preferredTurns: Int?,
        maxTurns: Int,
        at: LocalDateTime,
    ): SpeakingSession {
        val nextTurnCount: Int = currentChildTurnCount + 1
        val nextTurnsWithoutNewElement: Int = nextTurnsWithoutNewElement(hasNewElement)
        val nextLowInformationTurns: Int = nextLowInformationTurns(validity)
        val endReason: SceneEndReason? =
            resolveEndReason(missingElements, nextTurnCount, preferredTurns, maxTurns)
        val mode: ResponseMode =
            resolveMode(endReason, nextTurnsWithoutNewElement, nextLowInformationTurns, missingElements)

        return copy(
            currentChildTurnCount = nextTurnCount,
            turnsWithoutNewElement = nextTurnsWithoutNewElement,
            consecutiveLowInformationTurns = nextLowInformationTurns,
            sceneGoalMet = endReason == SceneEndReason.GOAL_MET,
            sceneEndReason = endReason,
            lastResponseMode = mode,
            lastGuidanceTarget = resolveGuidanceTarget(mode, missingElements),
            lastActivityAt = at,
        )
    }

    private fun nextTurnsWithoutNewElement(hasNewElement: Boolean): Int {
        if (hasNewElement) {
            return 0
        }

        return turnsWithoutNewElement + 1
    }

    private fun nextLowInformationTurns(validity: UtteranceValidity): Int {
        if (validity in LOW_INFORMATION_VALIDITIES) {
            return consecutiveLowInformationTurns + 1
        }

        return 0
    }

    private fun resolveEndReason(
        missingElements: List<ThinkingElement>,
        turnCount: Int,
        preferredTurns: Int?,
        maxTurns: Int,
    ): SceneEndReason? {
        val minTurns: Int = preferredTurns ?: MIN_PREFERRED_TURNS
        if (missingElements.isEmpty() && turnCount >= minTurns) {
            return SceneEndReason.GOAL_MET
        }
        if (turnCount >= maxTurns) {
            return SceneEndReason.MAX_TURNS
        }

        return null
    }

    private fun resolveMode(
        endReason: SceneEndReason?,
        turnsWithoutNewElement: Int,
        lowInformationTurns: Int,
        missingElements: List<ThinkingElement>,
    ): ResponseMode {
        if (endReason != null) {
            return ResponseMode.CLOSING
        }
        if (lastResponseMode == ResponseMode.GUIDED) {
            return ResponseMode.NORMAL
        }
        if (shouldGuide(turnsWithoutNewElement, lowInformationTurns, missingElements)) {
            return ResponseMode.GUIDED
        }

        return ResponseMode.NORMAL
    }

    private fun shouldGuide(
        turnsWithoutNewElement: Int,
        lowInformationTurns: Int,
        missingElements: List<ThinkingElement>,
    ): Boolean {
        if (missingElements.isEmpty()) {
            return false
        }

        return turnsWithoutNewElement >= GUIDE_STREAK_THRESHOLD || lowInformationTurns >= GUIDE_STREAK_THRESHOLD
    }

    private fun resolveGuidanceTarget(
        mode: ResponseMode,
        missingElements: List<ThinkingElement>,
    ): ThinkingElement? {
        if (mode != ResponseMode.GUIDED) {
            return null
        }

        return missingElements.firstOrNull()
    }

    companion object {
        private const val MIN_PREFERRED_TURNS: Int = 1
        private const val GUIDE_STREAK_THRESHOLD: Int = 2
        private val LOW_INFORMATION_VALIDITIES: Set<UtteranceValidity> = setOf(
            UtteranceValidity.SHORT,
            UtteranceValidity.UNCLEAR,
            UtteranceValidity.OFF_TOPIC,
        )

        fun start(childId: ChildId, storyId: StoryId, at: LocalDateTime): SpeakingSession = SpeakingSession(
            sessionId = SpeakingSessionId(UuidGenerator.generate()),
            childId = childId,
            storyId = storyId,
            status = SessionStatus.IN_PROGRESS,
            currentSceneId = null,
            startedAt = at,
            lastActivityAt = at,
        )
    }
}
