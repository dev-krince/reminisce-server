package com.krince.reminisce.application.service.speakingsession

import com.krince.reminisce.application.port.access.child.ChildAccessPort
import com.krince.reminisce.application.port.access.childconsent.ChildConsentAccessPort
import com.krince.reminisce.application.port.access.story.StoryAccessPort
import com.krince.reminisce.application.port.`in`.speakingsession.command.StartSpeakingSessionCommand
import com.krince.reminisce.application.port.out.speakingsession.CommandSpeakingSessionPort
import com.krince.reminisce.application.port.out.speakingsession.LoadSpeakingSessionPort
import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.speakingsession.SpeakingSession
import com.krince.reminisce.domain.model.speakingsession.vo.SessionStatus
import com.krince.reminisce.domain.model.speakingsession.vo.SpeakingSessionId
import com.krince.reminisce.domain.model.story.vo.StoryId
import com.krince.reminisce.domain.model.user.vo.UserId
import com.krince.reminisce.shared.exception.BusinessRuleViolationException
import com.krince.reminisce.shared.exception.NotFoundException
import com.krince.reminisce.shared.response.ExceptionResponseCode.CONSENT_REQUIRED
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND_CHILD
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND_STORY
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
@DisplayName("StartSpeakingSessionApplicationService 단위테스트")
class StartSpeakingSessionApplicationServiceTest : FunSpec({

    val childAccessPort = mockk<ChildAccessPort>()
    val childConsentAccessPort = mockk<ChildConsentAccessPort>()
    val storyAccessPort = mockk<StoryAccessPort>()
    val loadSpeakingSessionPort = mockk<LoadSpeakingSessionPort>()
    val commandSpeakingSessionPort = mockk<CommandSpeakingSessionPort>()
    val fixedInstant = Instant.parse("2026-06-01T00:00:00Z")
    val fixedClock = Clock.fixed(fixedInstant, ZoneOffset.UTC)
    val service = StartSpeakingSessionApplicationService(
        childAccessPort = childAccessPort,
        childConsentAccessPort = childConsentAccessPort,
        storyAccessPort = storyAccessPort,
        loadSpeakingSessionPort = loadSpeakingSessionPort,
        commandSpeakingSessionPort = commandSpeakingSessionPort,
        clock = fixedClock,
    )

    beforeEach { clearAllMocks() }

    val guardianIdStr = "guardian-uuid-1"
    val guardianId = UserId(guardianIdStr)
    val otherGuardianId = UserId("guardian-uuid-2")
    val childIdStr = "child-uuid-1"
    val childId = ChildId(childIdStr)
    val storyIdStr = "story-uuid-1"
    val storyId = StoryId(storyIdStr)

    fun command(): StartSpeakingSessionCommand =
        StartSpeakingSessionCommand(guardianId = guardianIdStr, childId = childIdStr, storyId = storyIdStr)

    fun inProgressSession(sessionIdStr: String): SpeakingSession = SpeakingSession(
        sessionId = SpeakingSessionId(sessionIdStr),
        childId = childId,
        storyId = storyId,
        status = SessionStatus.IN_PROGRESS,
        currentSceneId = null,
        startedAt = LocalDateTime.now(fixedClock).minusMinutes(5),
        lastActivityAt = LocalDateTime.now(fixedClock).minusMinutes(1),
        createdDate = LocalDateTime.now(fixedClock).minusMinutes(5),
        modifiedDate = LocalDateTime.now(fixedClock).minusMinutes(1),
    )

    context("성공") {
        test("동의 있는 내 아이와 공개 이야기면 새 세션을 저장하고 created=true로 반환한다") {
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every { childConsentAccessPort.hasActiveConsent(childId) } returns true
            every { storyAccessPort.existsPublished(storyId) } returns true
            every { loadSpeakingSessionPort.findInProgress(childId, storyId) } returns null
            val savedSlot = slot<SpeakingSession>()
            every { commandSpeakingSessionPort.save(capture(savedSlot)) } answers { savedSlot.captured }

            val result = service.execute(command())

            result.created shouldBe true
            result.status shouldBe SessionStatus.IN_PROGRESS.name
            result.currentSceneId shouldBe null
            result.childId shouldBe childIdStr
            result.storyId shouldBe storyIdStr
            result.startedAt shouldBe LocalDateTime.now(fixedClock)
            savedSlot.captured.status shouldBe SessionStatus.IN_PROGRESS
            savedSlot.captured.startedAt shouldBe LocalDateTime.now(fixedClock)
            savedSlot.captured.lastActivityAt shouldBe LocalDateTime.now(fixedClock)
            savedSlot.captured.currentSceneId shouldBe null
            verify(exactly = 1) { commandSpeakingSessionPort.save(any()) }
        }

        test("이미 진행 중인 세션이 있으면 새로 저장하지 않고 created=false로 기존 세션을 반환한다") {
            val existingSessionId = "existing-session-uuid"
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every { childConsentAccessPort.hasActiveConsent(childId) } returns true
            every { storyAccessPort.existsPublished(storyId) } returns true
            every { loadSpeakingSessionPort.findInProgress(childId, storyId) } returns inProgressSession(existingSessionId)

            val result = service.execute(command())

            result.created shouldBe false
            result.sessionId shouldBe existingSessionId
            result.status shouldBe SessionStatus.IN_PROGRESS.name
            verify(exactly = 0) { commandSpeakingSessionPort.save(any()) }
        }
    }

    context("게이트 실패") {
        test("아이가 존재하지 않으면 NOT_FOUND_CHILD를 던지고 저장하지 않는다") {
            every { childAccessPort.findGuardianId(childId) } returns null

            val exception = shouldThrow<NotFoundException> { service.execute(command()) }

            exception.exceptionResponseCode shouldBe NOT_FOUND_CHILD
            verify(exactly = 0) { commandSpeakingSessionPort.save(any()) }
        }

        test("타 보호자의 아이면 NOT_FOUND_CHILD로 은닉하고 저장하지 않는다") {
            every { childAccessPort.findGuardianId(childId) } returns otherGuardianId

            val exception = shouldThrow<NotFoundException> { service.execute(command()) }

            exception.exceptionResponseCode shouldBe NOT_FOUND_CHILD
            verify(exactly = 0) { commandSpeakingSessionPort.save(any()) }
        }

        test("동의가 없으면 CONSENT_REQUIRED를 던지고 저장하지 않는다") {
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every { childConsentAccessPort.hasActiveConsent(childId) } returns false

            val exception = shouldThrow<BusinessRuleViolationException> { service.execute(command()) }

            exception.exceptionResponseCode shouldBe CONSENT_REQUIRED
            verify(exactly = 0) { storyAccessPort.existsPublished(any()) }
            verify(exactly = 0) { commandSpeakingSessionPort.save(any()) }
        }

        test("공개 이야기가 아니면 NOT_FOUND_STORY를 던지고 저장하지 않는다") {
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every { childConsentAccessPort.hasActiveConsent(childId) } returns true
            every { storyAccessPort.existsPublished(storyId) } returns false

            val exception = shouldThrow<NotFoundException> { service.execute(command()) }

            exception.exceptionResponseCode shouldBe NOT_FOUND_STORY
            verify(exactly = 0) { loadSpeakingSessionPort.findInProgress(any(), any()) }
            verify(exactly = 0) { commandSpeakingSessionPort.save(any()) }
        }
    }
})
