package com.krince.reminisce.domain.model.speakingsession

import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.speakingsession.vo.ResponseMode
import com.krince.reminisce.domain.model.speakingsession.vo.SceneEndReason
import com.krince.reminisce.domain.model.speakingsession.vo.SessionStatus
import com.krince.reminisce.domain.model.speakingsession.vo.SpeakingSessionId
import com.krince.reminisce.domain.model.story.vo.StoryId
import com.krince.reminisce.domain.model.story.vo.ThinkingElement
import com.krince.reminisce.domain.model.utteranceanalysis.vo.UtteranceValidity
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.time.LocalDateTime

@Tags("test", "unitTest")
@DisplayName("SpeakingSession.advanceTurn 단위테스트")
class SpeakingSessionAdvanceTurnTest : FunSpec({

    val at = LocalDateTime.of(2026, 6, 1, 10, 10, 0)
    val perspective = ThinkingElement.PERSPECTIVE
    val emotion = ThinkingElement.EMOTION
    val bigMaxTurns = 99

    fun session(
        currentChildTurnCount: Int = 0,
        turnsWithoutNewElement: Int = 0,
        consecutiveLowInformationTurns: Int = 0,
        lastResponseMode: ResponseMode? = null,
    ): SpeakingSession = SpeakingSession(
        sessionId = SpeakingSessionId("session-uuid-1"),
        childId = ChildId("child-uuid-1"),
        storyId = StoryId("story-uuid-1"),
        status = SessionStatus.IN_PROGRESS,
        currentSceneId = "scene-uuid-1",
        startedAt = at.minusMinutes(5),
        lastActivityAt = at.minusMinutes(1),
        currentChildTurnCount = currentChildTurnCount,
        turnsWithoutNewElement = turnsWithoutNewElement,
        consecutiveLowInformationTurns = consecutiveLowInformationTurns,
        lastResponseMode = lastResponseMode,
    )

    context("턴 카운터") {
        test("신규 요소가 있으면 turnsWithoutNewElement가 0으로 리셋되고 턴 수가 증가한다") {
            val updated = session(currentChildTurnCount = 1, turnsWithoutNewElement = 1).advanceTurn(
                hasNewElement = true,
                validity = UtteranceValidity.VALID,
                missingElements = listOf(perspective),
                preferredTurns = null,
                maxTurns = bigMaxTurns,
                at = at,
            )

            updated.currentChildTurnCount shouldBe 2
            updated.turnsWithoutNewElement shouldBe 0
        }

        test("신규 요소가 없으면 turnsWithoutNewElement가 1 증가한다") {
            val updated = session(turnsWithoutNewElement = 1).advanceTurn(
                hasNewElement = false,
                validity = UtteranceValidity.VALID,
                missingElements = listOf(perspective),
                preferredTurns = null,
                maxTurns = bigMaxTurns,
                at = at,
            )

            updated.turnsWithoutNewElement shouldBe 2
        }

        test("저정보(SHORT/UNCLEAR/OFF_TOPIC)면 연속 카운트가 증가한다") {
            listOf(UtteranceValidity.SHORT, UtteranceValidity.UNCLEAR, UtteranceValidity.OFF_TOPIC).forEach { validity ->
                val updated = session(consecutiveLowInformationTurns = 1).advanceTurn(
                    hasNewElement = false,
                    validity = validity,
                    missingElements = listOf(perspective),
                    preferredTurns = null,
                    maxTurns = bigMaxTurns,
                    at = at,
                )

                updated.consecutiveLowInformationTurns shouldBe 2
            }
        }

        test("VALID면 저정보 연속 카운트가 0으로 리셋된다") {
            val updated = session(consecutiveLowInformationTurns = 2).advanceTurn(
                hasNewElement = false,
                validity = UtteranceValidity.VALID,
                missingElements = listOf(perspective),
                preferredTurns = null,
                maxTurns = bigMaxTurns,
                at = at,
            )

            updated.consecutiveLowInformationTurns shouldBe 0
        }

        test("PLAYFUL은 저정보가 아니므로 연속 카운트가 0으로 리셋된다") {
            val updated = session(consecutiveLowInformationTurns = 2).advanceTurn(
                hasNewElement = false,
                validity = UtteranceValidity.PLAYFUL,
                missingElements = listOf(perspective),
                preferredTurns = null,
                maxTurns = bigMaxTurns,
                at = at,
            )

            updated.consecutiveLowInformationTurns shouldBe 0
        }
    }

    context("종료 판정") {
        test("권장턴 이상이고 필수 요소가 모두 충족되면 GOAL_MET이고 목표 충족이다") {
            val updated = session(currentChildTurnCount = 1).advanceTurn(
                hasNewElement = true,
                validity = UtteranceValidity.VALID,
                missingElements = emptyList(),
                preferredTurns = 2,
                maxTurns = bigMaxTurns,
                at = at,
            )

            updated.sceneEndReason shouldBe SceneEndReason.GOAL_MET
            updated.sceneGoalMet shouldBe true
            updated.lastResponseMode shouldBe ResponseMode.CLOSING
        }

        test("권장턴이 null이면 요소를 다 채워도 최소 3턴 전에는 GOAL_MET/CLOSING이 아니고 진행을 유지한다") {
            listOf(0, 1).forEach { beforeTurnCount ->
                val updated = session(currentChildTurnCount = beforeTurnCount).advanceTurn(
                    hasNewElement = true,
                    validity = UtteranceValidity.VALID,
                    missingElements = emptyList(),
                    preferredTurns = null,
                    maxTurns = bigMaxTurns,
                    at = at,
                )

                updated.sceneEndReason shouldBe null
                updated.sceneGoalMet shouldBe false
                updated.lastResponseMode shouldNotBe ResponseMode.CLOSING
            }
        }

        test("권장턴이 null이면 요소를 다 채운 3턴째에 GOAL_MET·CLOSING이 된다") {
            val updated = session(currentChildTurnCount = 2).advanceTurn(
                hasNewElement = true,
                validity = UtteranceValidity.VALID,
                missingElements = emptyList(),
                preferredTurns = null,
                maxTurns = bigMaxTurns,
                at = at,
            )

            updated.sceneEndReason shouldBe SceneEndReason.GOAL_MET
            updated.sceneGoalMet shouldBe true
            updated.lastResponseMode shouldBe ResponseMode.CLOSING
        }

        test("최대턴에 도달하면 필수 요소가 남아도 MAX_TURNS로 종료한다") {
            val updated = session(currentChildTurnCount = 3).advanceTurn(
                hasNewElement = false,
                validity = UtteranceValidity.VALID,
                missingElements = listOf(perspective),
                preferredTurns = null,
                maxTurns = 4,
                at = at,
            )

            updated.sceneEndReason shouldBe SceneEndReason.MAX_TURNS
            updated.sceneGoalMet shouldBe false
            updated.lastResponseMode shouldBe ResponseMode.CLOSING
        }

        test("GOAL_MET과 MAX_TURNS 조건이 동시에 성립하면 GOAL_MET이 우선한다") {
            val updated = session(currentChildTurnCount = 3).advanceTurn(
                hasNewElement = true,
                validity = UtteranceValidity.VALID,
                missingElements = emptyList(),
                preferredTurns = 4,
                maxTurns = 4,
                at = at,
            )

            updated.sceneEndReason shouldBe SceneEndReason.GOAL_MET
            updated.sceneGoalMet shouldBe true
        }

        test("권장턴 미달이고 최대턴 미달이면 종료하지 않는다") {
            val updated = session(currentChildTurnCount = 0).advanceTurn(
                hasNewElement = true,
                validity = UtteranceValidity.VALID,
                missingElements = emptyList(),
                preferredTurns = 3,
                maxTurns = 4,
                at = at,
            )

            updated.sceneEndReason shouldBe null
            updated.sceneGoalMet shouldBe false
        }
    }

    context("응답 모드") {
        test("종료면 CLOSING이 되고 유도 대상은 없다") {
            val updated = session(currentChildTurnCount = 3).advanceTurn(
                hasNewElement = false,
                validity = UtteranceValidity.VALID,
                missingElements = listOf(perspective),
                preferredTurns = null,
                maxTurns = 4,
                at = at,
            )

            updated.lastResponseMode shouldBe ResponseMode.CLOSING
            updated.lastGuidanceTarget shouldBe null
        }

        test("신규 요소 없는 발화가 2연속이면 GUIDED가 되고 유도 대상은 missing 첫 요소다") {
            val updated = session(currentChildTurnCount = 1, turnsWithoutNewElement = 1).advanceTurn(
                hasNewElement = false,
                validity = UtteranceValidity.VALID,
                missingElements = listOf(perspective, emotion),
                preferredTurns = null,
                maxTurns = bigMaxTurns,
                at = at,
            )

            updated.lastResponseMode shouldBe ResponseMode.GUIDED
            updated.lastGuidanceTarget shouldBe perspective
        }

        test("저정보 발화가 2연속이면 GUIDED가 된다") {
            val updated = session(currentChildTurnCount = 1, consecutiveLowInformationTurns = 1).advanceTurn(
                hasNewElement = false,
                validity = UtteranceValidity.SHORT,
                missingElements = listOf(perspective),
                preferredTurns = null,
                maxTurns = bigMaxTurns,
                at = at,
            )

            updated.lastResponseMode shouldBe ResponseMode.GUIDED
            updated.lastGuidanceTarget shouldBe perspective
        }

        test("직전 모드가 GUIDED면 트리거가 충족돼도 이번엔 NORMAL로 강제되고 유도 대상은 없다") {
            val updated = session(
                currentChildTurnCount = 1,
                turnsWithoutNewElement = 1,
                lastResponseMode = ResponseMode.GUIDED,
            ).advanceTurn(
                hasNewElement = false,
                validity = UtteranceValidity.VALID,
                missingElements = listOf(perspective),
                preferredTurns = null,
                maxTurns = bigMaxTurns,
                at = at,
            )

            updated.lastResponseMode shouldBe ResponseMode.NORMAL
            updated.lastGuidanceTarget shouldBe null
        }

        test("트리거가 없으면 NORMAL이 된다") {
            val updated = session(currentChildTurnCount = 1).advanceTurn(
                hasNewElement = true,
                validity = UtteranceValidity.VALID,
                missingElements = listOf(perspective),
                preferredTurns = null,
                maxTurns = bigMaxTurns,
                at = at,
            )

            updated.lastResponseMode shouldBe ResponseMode.NORMAL
            updated.lastGuidanceTarget shouldBe null
        }

        test("트리거가 충족돼도 유도할 missing이 없으면 NORMAL이고 유도 대상은 없다") {
            val updated = session(currentChildTurnCount = 1, turnsWithoutNewElement = 1).advanceTurn(
                hasNewElement = false,
                validity = UtteranceValidity.VALID,
                missingElements = emptyList(),
                preferredTurns = 99,
                maxTurns = bigMaxTurns,
                at = at,
            )

            updated.lastResponseMode shouldBe ResponseMode.NORMAL
            updated.lastGuidanceTarget shouldBe null
        }
    }

    test("진행은 status와 currentSceneId를 유지한다") {
        val original = session(currentChildTurnCount = 1)

        val updated = original.advanceTurn(
            hasNewElement = true,
            validity = UtteranceValidity.VALID,
            missingElements = emptyList(),
            preferredTurns = null,
            maxTurns = bigMaxTurns,
            at = at,
        )

        updated.status shouldBe SessionStatus.IN_PROGRESS
        updated.currentSceneId shouldBe original.currentSceneId
        updated.lastActivityAt shouldBe at
    }
})
