package com.krince.reminisce.domain.model.speakingsession

import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.speakingsession.vo.ResponseMode
import com.krince.reminisce.domain.model.speakingsession.vo.SceneEndReason
import com.krince.reminisce.domain.model.speakingsession.vo.SessionStatus
import com.krince.reminisce.domain.model.speakingsession.vo.SpeakingSessionId
import com.krince.reminisce.domain.model.story.vo.StoryId
import com.krince.reminisce.domain.model.story.vo.ThinkingElement
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDateTime

@Tags("test", "unitTest")
@DisplayName("SpeakingSession.transitionToScene·enterPostActivity 단위테스트")
class SpeakingSessionTransitionTest : FunSpec({

    val startedAt = LocalDateTime.of(2026, 6, 1, 10, 0, 0)
    val lastActivityAt = LocalDateTime.of(2026, 6, 1, 10, 5, 0)
    val transitionAt = LocalDateTime.of(2026, 6, 1, 10, 10, 0)
    val currentSceneId = "scene-uuid-1"
    val nextSceneId = "scene-uuid-2"

    fun advancedSceneSession(): SpeakingSession = SpeakingSession(
        sessionId = SpeakingSessionId("session-uuid-1"),
        childId = ChildId("child-uuid-1"),
        storyId = StoryId("story-uuid-1"),
        status = SessionStatus.IN_PROGRESS,
        currentSceneId = currentSceneId,
        startedAt = startedAt,
        lastActivityAt = lastActivityAt,
        accumulatedElements = listOf(ThinkingElement.EMOTION, ThinkingElement.PERSPECTIVE),
        currentChildTurnCount = 3,
        turnsWithoutNewElement = 2,
        consecutiveLowInformationTurns = 1,
        sceneGoalMet = true,
        sceneEndReason = SceneEndReason.MAX_TURNS,
        lastResponseMode = ResponseMode.CLOSING,
        lastGuidanceTarget = ThinkingElement.PERSPECTIVE,
    )

    test("transitionToScene은 currentSceneId를 다음 장면으로 바꾸고 lastActivityAt을 갱신한다") {
        val transitioned = advancedSceneSession().transitionToScene(nextSceneId, transitionAt)

        transitioned.currentSceneId shouldBe nextSceneId
        transitioned.lastActivityAt shouldBe transitionAt
    }

    test("transitionToScene은 장면 범위 상태를 전부 0·빈값으로 초기화한다") {
        val transitioned = advancedSceneSession().transitionToScene(nextSceneId, transitionAt)

        transitioned.accumulatedElements shouldBe emptyList()
        transitioned.currentChildTurnCount shouldBe 0
        transitioned.turnsWithoutNewElement shouldBe 0
        transitioned.consecutiveLowInformationTurns shouldBe 0
        transitioned.sceneGoalMet shouldBe false
        transitioned.sceneEndReason shouldBe null
        transitioned.lastResponseMode shouldBe null
        transitioned.lastGuidanceTarget shouldBe null
    }

    test("transitionToScene은 status를 IN_PROGRESS로 유지하고 식별 필드를 보존한다") {
        val original = advancedSceneSession()

        val transitioned = original.transitionToScene(nextSceneId, transitionAt)

        transitioned.status shouldBe SessionStatus.IN_PROGRESS
        transitioned.sessionId shouldBe original.sessionId
        transitioned.childId shouldBe original.childId
        transitioned.storyId shouldBe original.storyId
        transitioned.startedAt shouldBe original.startedAt
    }

    test("enterPostActivity는 status를 POST_ACTIVITY로 바꾸고 lastActivityAt을 갱신한다") {
        val original = advancedSceneSession()

        val entered = original.enterPostActivity(transitionAt)

        entered.status shouldBe SessionStatus.POST_ACTIVITY
        entered.lastActivityAt shouldBe transitionAt
        entered.currentSceneId shouldBe original.currentSceneId
    }
})
