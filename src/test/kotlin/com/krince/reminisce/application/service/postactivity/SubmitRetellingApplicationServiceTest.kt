package com.krince.reminisce.application.service.postactivity

import com.krince.reminisce.application.port.access.child.ChildAccessPort
import com.krince.reminisce.application.port.`in`.postactivity.command.SubmitRetellingCommand
import com.krince.reminisce.application.port.out.postactivityresult.CommandPostActivityResultPort
import com.krince.reminisce.application.port.out.postactivityresult.LoadPostActivityResultPort
import com.krince.reminisce.application.port.out.speakingsession.CommandSpeakingSessionPort
import com.krince.reminisce.application.port.out.speakingsession.LoadSpeakingSessionPort
import com.krince.reminisce.application.port.out.stt.SttPort
import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.postactivityresult.PostActivityResult
import com.krince.reminisce.domain.model.postactivityresult.vo.PostActivityResultId
import com.krince.reminisce.domain.model.speakingsession.SpeakingSession
import com.krince.reminisce.domain.model.speakingsession.vo.SessionStatus
import com.krince.reminisce.domain.model.speakingsession.vo.SpeakingSessionId
import com.krince.reminisce.domain.model.story.vo.StoryId
import com.krince.reminisce.domain.model.user.vo.UserId
import com.krince.reminisce.shared.exception.BusinessRuleViolationException
import com.krince.reminisce.shared.exception.NotFoundException
import com.krince.reminisce.shared.response.ExceptionResponseCode.BUSINESS_RULE_VIOLATION
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND
import com.krince.reminisce.shared.response.ExceptionResponseCode.STT_TRANSCRIPTION_FAILED
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@Tags("test", "unitTest")
@DisplayName("SubmitRetellingApplicationService 단위테스트")
class SubmitRetellingApplicationServiceTest : FunSpec({

    val loadSpeakingSessionPort = mockk<LoadSpeakingSessionPort>()
    val childAccessPort = mockk<ChildAccessPort>()
    val loadPostActivityResultPort = mockk<LoadPostActivityResultPort>()
    val commandPostActivityResultPort = mockk<CommandPostActivityResultPort>()
    val commandSpeakingSessionPort = mockk<CommandSpeakingSessionPort>()
    val sttPort = mockk<SttPort>()
    val fixedInstant: Instant = Instant.parse("2024-06-01T10:00:00Z")
    val fixedClock: Clock = Clock.fixed(fixedInstant, ZoneId.of("UTC"))

    val service = SubmitRetellingApplicationService(
        loadSpeakingSessionPort = loadSpeakingSessionPort,
        childAccessPort = childAccessPort,
        loadPostActivityResultPort = loadPostActivityResultPort,
        commandPostActivityResultPort = commandPostActivityResultPort,
        commandSpeakingSessionPort = commandSpeakingSessionPort,
        sttPort = sttPort,
        clock = fixedClock,
    )

    beforeEach { clearAllMocks() }

    val sessionIdStr = "session-uuid-1"
    val guardianIdStr = "guardian-uuid-1"
    val guardianId = UserId(guardianIdStr)
    val otherGuardianId = UserId("guardian-uuid-2")
    val childId = ChildId("child-uuid-1")
    val storyId = StoryId("story-uuid-1")
    val validAudio = "방귀쟁이 며느리는 시아버지 덕분에 방귀를 뀔 수 있었어요"
    val transcript = "방귀쟁이 며느리는 시아버지 덕분에 방귀를 뀔 수 있었어요"

    fun command(audio: String = validAudio): SubmitRetellingCommand =
        SubmitRetellingCommand(sessionId = sessionIdStr, guardianId = guardianIdStr, audio = audio)

    fun session(status: SessionStatus = SessionStatus.POST_ACTIVITY): SpeakingSession = SpeakingSession(
        sessionId = SpeakingSessionId(sessionIdStr),
        childId = childId,
        storyId = storyId,
        status = status,
        startedAt = LocalDateTime.now().minusMinutes(10),
        lastActivityAt = LocalDateTime.now().minusMinutes(1),
    )

    fun savedResult(
        submittedOrder: List<String> = listOf("card-a", "card-b", "card-c"),
        isOrderCorrect: Boolean? = true,
        attemptCount: Int = 1,
    ): PostActivityResult = PostActivityResult(
        id = PostActivityResultId("result-uuid-1"),
        sessionId = SpeakingSessionId(sessionIdStr),
        submittedOrder = submittedOrder,
        isOrderCorrect = isOrderCorrect,
        attemptCount = attemptCount,
    )

    context("게이트 실패") {
        test("세션이 없으면 NOT_FOUND를 던지고 저장하지 않는다") {
            every { loadSpeakingSessionPort.findById(SpeakingSessionId(sessionIdStr)) } returns null

            val exception = shouldThrow<NotFoundException> { service.execute(command()) }

            exception.exceptionResponseCode shouldBe NOT_FOUND
            verify(exactly = 0) { commandPostActivityResultPort.save(any()) }
            verify(exactly = 0) { commandSpeakingSessionPort.save(any()) }
        }

        test("타 보호자의 아이 세션이면 NOT_FOUND로 은닉하고 저장하지 않는다") {
            every { loadSpeakingSessionPort.findById(SpeakingSessionId(sessionIdStr)) } returns session()
            every { childAccessPort.findGuardianId(childId) } returns otherGuardianId

            val exception = shouldThrow<NotFoundException> { service.execute(command()) }

            exception.exceptionResponseCode shouldBe NOT_FOUND
            verify(exactly = 0) { commandPostActivityResultPort.save(any()) }
            verify(exactly = 0) { commandSpeakingSessionPort.save(any()) }
        }

        test("status가 POST_ACTIVITY가 아니면 BUSINESS_RULE_VIOLATION을 던지고 저장하지 않는다") {
            every { loadSpeakingSessionPort.findById(SpeakingSessionId(sessionIdStr)) } returns session(SessionStatus.IN_PROGRESS)
            every { childAccessPort.findGuardianId(childId) } returns guardianId

            val exception = shouldThrow<BusinessRuleViolationException> { service.execute(command()) }

            exception.exceptionResponseCode shouldBe BUSINESS_RULE_VIOLATION
            verify(exactly = 0) { commandPostActivityResultPort.save(any()) }
            verify(exactly = 0) { commandSpeakingSessionPort.save(any()) }
        }

        test("post_activity_result가 없으면 BUSINESS_RULE_VIOLATION을 던지고 저장하지 않는다") {
            every { loadSpeakingSessionPort.findById(SpeakingSessionId(sessionIdStr)) } returns session()
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every { loadPostActivityResultPort.findBySession(SpeakingSessionId(sessionIdStr)) } returns null

            val exception = shouldThrow<BusinessRuleViolationException> { service.execute(command()) }

            exception.exceptionResponseCode shouldBe BUSINESS_RULE_VIOLATION
            verify(exactly = 0) { commandPostActivityResultPort.save(any()) }
            verify(exactly = 0) { commandSpeakingSessionPort.save(any()) }
        }

        test("isOrderCorrect가 false이면 BUSINESS_RULE_VIOLATION을 던지고 저장하지 않는다") {
            every { loadSpeakingSessionPort.findById(SpeakingSessionId(sessionIdStr)) } returns session()
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every {
                loadPostActivityResultPort.findBySession(SpeakingSessionId(sessionIdStr))
            } returns savedResult(isOrderCorrect = false)

            val exception = shouldThrow<BusinessRuleViolationException> { service.execute(command()) }

            exception.exceptionResponseCode shouldBe BUSINESS_RULE_VIOLATION
            verify(exactly = 0) { commandPostActivityResultPort.save(any()) }
            verify(exactly = 0) { commandSpeakingSessionPort.save(any()) }
        }

        test("isOrderCorrect가 null이면 BUSINESS_RULE_VIOLATION을 던지고 저장하지 않는다") {
            every { loadSpeakingSessionPort.findById(SpeakingSessionId(sessionIdStr)) } returns session()
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every {
                loadPostActivityResultPort.findBySession(SpeakingSessionId(sessionIdStr))
            } returns savedResult(isOrderCorrect = null)

            val exception = shouldThrow<BusinessRuleViolationException> { service.execute(command()) }

            exception.exceptionResponseCode shouldBe BUSINESS_RULE_VIOLATION
            verify(exactly = 0) { commandPostActivityResultPort.save(any()) }
            verify(exactly = 0) { commandSpeakingSessionPort.save(any()) }
        }

        test("STT가 null을 반환하면 STT_TRANSCRIPTION_FAILED를 던지고 저장하지 않는다") {
            every { loadSpeakingSessionPort.findById(SpeakingSessionId(sessionIdStr)) } returns session()
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every {
                loadPostActivityResultPort.findBySession(SpeakingSessionId(sessionIdStr))
            } returns savedResult(isOrderCorrect = true)
            every { sttPort.transcribe(any()) } returns null

            val exception = shouldThrow<BusinessRuleViolationException> { service.execute(command()) }

            exception.exceptionResponseCode shouldBe STT_TRANSCRIPTION_FAILED
            verify(exactly = 0) { commandPostActivityResultPort.save(any()) }
            verify(exactly = 0) { commandSpeakingSessionPort.save(any()) }
        }
    }

    context("성공") {
        test("POST_ACTIVITY·정답결과·유효 audio이면 completeWith·complete로 2회 저장하고 status=COMPLETED를 반환한다") {
            val existingResult = savedResult(isOrderCorrect = true)
            every { loadSpeakingSessionPort.findById(SpeakingSessionId(sessionIdStr)) } returns session()
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every {
                loadPostActivityResultPort.findBySession(SpeakingSessionId(sessionIdStr))
            } returns existingResult
            every { sttPort.transcribe(validAudio) } returns transcript
            every { commandPostActivityResultPort.save(any()) } answers { firstArg() }
            every { commandSpeakingSessionPort.save(any()) } answers { firstArg() }

            val result = service.execute(command())

            result.retellingText shouldBe transcript
            result.status shouldBe SessionStatus.COMPLETED
            verify(exactly = 1) { commandPostActivityResultPort.save(any()) }
            verify(exactly = 1) { commandSpeakingSessionPort.save(any()) }
        }
    }
})
