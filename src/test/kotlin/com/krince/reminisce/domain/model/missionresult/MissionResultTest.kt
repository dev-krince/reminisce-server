package com.krince.reminisce.domain.model.missionresult

import com.krince.reminisce.domain.model.missionresult.vo.MissionResultId
import com.krince.reminisce.domain.model.speakingsession.vo.SpeakingSessionId
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import java.time.LocalDateTime

@Tags("test", "unitTest")
@DisplayName("MissionResult 단위테스트")
class MissionResultTest : FunSpec({

    val sessionId = SpeakingSessionId("session-1")
    val sceneId = "sc_banggui_16"
    val now = LocalDateTime.of(2026, 8, 16, 10, 0)

    context("firstSubmission") {
        test("완료로 첫 제출하면 completed=true·attemptCount=1·completedAt이 채워진다") {
            val result = MissionResult.firstSubmission(sessionId, sceneId, completed = true, at = now)

            result.completed shouldBe true
            result.attemptCount shouldBe 1
            result.completedAt shouldBe now
        }

        test("미완료로 첫 제출하면 completed=false·attemptCount=1·completedAt이 null이다") {
            val result = MissionResult.firstSubmission(sessionId, sceneId, completed = false, at = now)

            result.completed shouldBe false
            result.attemptCount shouldBe 1
            result.completedAt shouldBe null
        }
    }

    context("resubmit") {
        val existing = MissionResult(
            id = MissionResultId("result-1"),
            sessionId = sessionId,
            sceneId = sceneId,
            completed = false,
            attemptCount = 1,
        )

        test("오답으로 재제출하면 attemptCount가 1 증가하고 미완료로 남는다") {
            val resubmitted = existing.resubmit(passed = false, at = now)

            resubmitted.attemptCount shouldBe 2
            resubmitted.completed shouldBe false
            resubmitted.completedAt shouldBe null
        }

        test("정답으로 재제출하면 completed=true·completedAt이 채워진다") {
            val resubmitted = existing.resubmit(passed = true, at = now)

            resubmitted.completed shouldBe true
            resubmitted.completedAt shouldBe now
        }

        test("이미 완료된 결과를 오답으로 재제출해도 completed=true·completedAt이 유지된다") {
            val completedAt = now.minusMinutes(5)
            val completedResult = existing.copy(completed = true, completedAt = completedAt)

            val resubmitted = completedResult.resubmit(passed = false, at = now)

            resubmitted.completed shouldBe true
            resubmitted.completedAt.shouldNotBeNull() shouldBe completedAt
            resubmitted.attemptCount shouldBe 2
        }
    }
})
