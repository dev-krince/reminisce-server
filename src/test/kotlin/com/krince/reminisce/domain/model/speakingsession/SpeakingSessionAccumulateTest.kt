package com.krince.reminisce.domain.model.speakingsession

import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.speakingsession.vo.SessionStatus
import com.krince.reminisce.domain.model.speakingsession.vo.SpeakingSessionId
import com.krince.reminisce.domain.model.story.vo.StoryId
import com.krince.reminisce.domain.model.story.vo.ThinkingElement
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import java.time.LocalDateTime

@Tags("test", "unitTest")
@DisplayName("SpeakingSession.accumulate 단위테스트")
class SpeakingSessionAccumulateTest : FunSpec({

    val startedAt = LocalDateTime.of(2026, 6, 1, 10, 0, 0)
    val lastActivityAt = LocalDateTime.of(2026, 6, 1, 10, 5, 0)
    val accumulatedAt = LocalDateTime.of(2026, 6, 1, 10, 10, 0)

    fun session(accumulatedElements: List<ThinkingElement> = emptyList()): SpeakingSession = SpeakingSession(
        sessionId = SpeakingSessionId("session-uuid-1"),
        childId = ChildId("child-uuid-1"),
        storyId = StoryId("story-uuid-1"),
        status = SessionStatus.IN_PROGRESS,
        currentSceneId = "scene-uuid-1",
        startedAt = startedAt,
        lastActivityAt = lastActivityAt,
        accumulatedElements = accumulatedElements,
    )

    test("빈 누적에 새 요소를 더하면 그대로 누적된다") {
        val updated = session().accumulate(listOf(ThinkingElement.EMOTION), accumulatedAt)

        updated.accumulatedElements shouldContainExactly listOf(ThinkingElement.EMOTION)
        updated.lastActivityAt shouldBe accumulatedAt
    }

    test("이미 있는 type을 다시 확인해도 중복 없이 합집합으로 누적된다") {
        val updated = session(listOf(ThinkingElement.EMOTION))
            .accumulate(listOf(ThinkingElement.EMOTION, ThinkingElement.PERSPECTIVE), accumulatedAt)

        updated.accumulatedElements shouldContainExactly listOf(ThinkingElement.EMOTION, ThinkingElement.PERSPECTIVE)
    }

    test("새 요소를 더해도 기존 누적은 보존된다") {
        val updated = session(listOf(ThinkingElement.EMOTION, ThinkingElement.REASON))
            .accumulate(listOf(ThinkingElement.PERSPECTIVE), accumulatedAt)

        updated.accumulatedElements shouldContainExactly listOf(
            ThinkingElement.EMOTION,
            ThinkingElement.REASON,
            ThinkingElement.PERSPECTIVE,
        )
    }

    test("누적은 세션의 나머지 필드를 보존하고 새 인스턴스를 반환한다") {
        val original = session(listOf(ThinkingElement.EMOTION))

        val updated = original.accumulate(listOf(ThinkingElement.PERSPECTIVE), accumulatedAt)

        updated.sessionId shouldBe original.sessionId
        updated.childId shouldBe original.childId
        updated.storyId shouldBe original.storyId
        updated.currentSceneId shouldBe original.currentSceneId
        updated.status shouldBe original.status
        original.accumulatedElements shouldContainExactly listOf(ThinkingElement.EMOTION)
    }
})
