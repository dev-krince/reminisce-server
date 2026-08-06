package com.krince.reminisce.application.service.speakingsession

import com.krince.reminisce.application.port.access.child.ChildAccessPort
import com.krince.reminisce.application.port.access.story.StoryAccessPort
import com.krince.reminisce.application.port.`in`.speakingsession.command.AdvanceSpeakingSceneCommand
import com.krince.reminisce.application.port.`in`.speakingsession.result.SpeakingSessionViewType
import com.krince.reminisce.application.port.out.speakingsession.CommandSpeakingSessionPort
import com.krince.reminisce.application.port.out.speakingsession.LoadSpeakingSessionPort
import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.speakingsession.SpeakingSession
import com.krince.reminisce.domain.model.speakingsession.vo.SessionStatus
import com.krince.reminisce.domain.model.speakingsession.vo.SpeakingSessionId
import com.krince.reminisce.domain.model.story.Scene
import com.krince.reminisce.domain.model.story.vo.SceneId
import com.krince.reminisce.domain.model.story.vo.SceneType
import com.krince.reminisce.domain.model.story.vo.StoryId
import com.krince.reminisce.domain.model.story.vo.ThinkingElement
import com.krince.reminisce.domain.model.user.vo.UserId
import com.krince.reminisce.shared.exception.BusinessRuleViolationException
import com.krince.reminisce.shared.exception.NotFoundException
import com.krince.reminisce.shared.response.ExceptionResponseCode.BUSINESS_RULE_VIOLATION
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

@Tags("test", "unitTest")
@DisplayName("AdvanceSpeakingSceneApplicationService 단위테스트")
class AdvanceSpeakingSceneApplicationServiceTest : FunSpec({

    val loadSpeakingSessionPort = mockk<LoadSpeakingSessionPort>()
    val commandSpeakingSessionPort = mockk<CommandSpeakingSessionPort>()
    val childAccessPort = mockk<ChildAccessPort>()
    val storyAccessPort = mockk<StoryAccessPort>()
    val fixedInstant = Instant.parse("2026-06-01T00:00:00Z")
    val fixedClock = Clock.fixed(fixedInstant, ZoneOffset.UTC)
    val service = AdvanceSpeakingSceneApplicationService(
        loadSpeakingSessionPort = loadSpeakingSessionPort,
        commandSpeakingSessionPort = commandSpeakingSessionPort,
        childAccessPort = childAccessPort,
        storyAccessPort = storyAccessPort,
        clock = fixedClock,
    )

    beforeEach { clearAllMocks() }

    val sessionIdStr = "session-uuid-1"
    val guardianIdStr = "guardian-uuid-1"
    val guardianId = UserId(guardianIdStr)
    val otherGuardianId = UserId("guardian-uuid-2")
    val childId = ChildId("child-uuid-1")
    val storyId = StoryId("story-uuid-1")
    val firstSceneIdStr = "scene-uuid-1"

    fun command(): AdvanceSpeakingSceneCommand =
        AdvanceSpeakingSceneCommand(sessionId = sessionIdStr, guardianId = guardianIdStr)

    fun session(currentSceneId: String?): SpeakingSession = SpeakingSession(
        sessionId = SpeakingSessionId(sessionIdStr),
        childId = childId,
        storyId = storyId,
        status = SessionStatus.IN_PROGRESS,
        currentSceneId = currentSceneId,
        startedAt = LocalDateTime.now(fixedClock).minusMinutes(5),
        lastActivityAt = LocalDateTime.now(fixedClock).minusMinutes(1),
    )

    fun firstScene(): Scene = Scene(
        sceneId = SceneId(firstSceneIdStr),
        storyId = storyId,
        sceneOrder = 1,
        sceneType = SceneType.DIALOGUE,
        sceneDescription = "대화 설명",
        characterName = "ch_x",
        characterDisplayName = "표시명",
        characterOpening = "여는 대사",
        characterClosing = "닫는 대사",
        sceneGoal = "목표",
        requiredElements = listOf(ThinkingElement.PERSPECTIVE),
        maxTurns = 4,
    )

    context("게이트 실패") {
        test("세션이 없으면 NOT_FOUND를 던지고 저장하지 않는다") {
            every { loadSpeakingSessionPort.findById(SpeakingSessionId(sessionIdStr)) } returns null

            val exception = shouldThrow<NotFoundException> { service.execute(command()) }

            exception.exceptionResponseCode shouldBe NOT_FOUND
            verify(exactly = 0) { commandSpeakingSessionPort.save(any()) }
        }

        test("타 보호자의 아이 세션이면 NOT_FOUND로 은닉하고 저장하지 않는다") {
            every { loadSpeakingSessionPort.findById(SpeakingSessionId(sessionIdStr)) } returns session(null)
            every { childAccessPort.findGuardianId(childId) } returns otherGuardianId

            val exception = shouldThrow<NotFoundException> { service.execute(command()) }

            exception.exceptionResponseCode shouldBe NOT_FOUND
            verify(exactly = 0) { commandSpeakingSessionPort.save(any()) }
        }

        test("이미 장면 위 세션이면 BUSINESS_RULE_VIOLATION을 던지고 저장하지 않는다") {
            every { loadSpeakingSessionPort.findById(SpeakingSessionId(sessionIdStr)) } returns session(firstSceneIdStr)
            every { childAccessPort.findGuardianId(childId) } returns guardianId

            val exception = shouldThrow<BusinessRuleViolationException> { service.execute(command()) }

            exception.exceptionResponseCode shouldBe BUSINESS_RULE_VIOLATION
            verify(exactly = 0) { commandSpeakingSessionPort.save(any()) }
        }
    }

    context("성공") {
        test("도입 상태 세션이면 첫 장면으로 이동해 저장하고 SCENE 뷰를 반환한다") {
            every { loadSpeakingSessionPort.findById(SpeakingSessionId(sessionIdStr)) } returns session(null)
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every { storyAccessPort.findFirstSceneId(storyId) } returns firstSceneIdStr
            every { storyAccessPort.findScene(storyId, firstSceneIdStr) } returns firstScene()
            val savedSlot = slot<SpeakingSession>()
            every { commandSpeakingSessionPort.save(capture(savedSlot)) } answers { savedSlot.captured }

            val result = service.execute(command())

            result.viewType shouldBe SpeakingSessionViewType.SCENE
            result.scene?.sceneId shouldBe firstSceneIdStr
            savedSlot.captured.currentSceneId shouldBe firstSceneIdStr
            savedSlot.captured.status shouldBe SessionStatus.IN_PROGRESS
            savedSlot.captured.lastActivityAt shouldBe LocalDateTime.now(fixedClock)
            verify(exactly = 1) { commandSpeakingSessionPort.save(any()) }
        }
    }
})
