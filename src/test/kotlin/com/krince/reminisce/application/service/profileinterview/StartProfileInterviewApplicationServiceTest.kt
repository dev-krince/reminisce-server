package com.krince.reminisce.application.service.profileinterview

import com.krince.reminisce.application.port.access.child.ChildAccessPort
import com.krince.reminisce.application.port.access.childconsent.ChildConsentAccessPort
import com.krince.reminisce.application.port.`in`.profileinterview.command.StartProfileInterviewCommand
import com.krince.reminisce.application.port.out.profileinterview.CommandInterviewMessagePort
import com.krince.reminisce.application.port.out.profileinterview.CommandProfileInterviewPort
import com.krince.reminisce.application.port.out.profileinterview.InterviewReplyPort
import com.krince.reminisce.application.port.out.profileinterview.InterviewTurnSettingsPort
import com.krince.reminisce.application.port.out.profileinterview.LoadInterviewMessagePort
import com.krince.reminisce.application.port.out.profileinterview.LoadProfileInterviewPort
import com.krince.reminisce.application.port.out.tts.QUMI_VOICE_PROFILE
import com.krince.reminisce.application.port.out.tts.TtsPort
import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.profileinterview.InterviewMessage
import com.krince.reminisce.domain.model.profileinterview.ProfileInterview
import com.krince.reminisce.domain.model.profileinterview.vo.InterviewStage
import com.krince.reminisce.domain.model.user.vo.UserId
import com.krince.reminisce.shared.exception.BusinessRuleViolationException
import com.krince.reminisce.shared.exception.NotFoundException
import com.krince.reminisce.shared.response.ExceptionResponseCode.CONSENT_REQUIRED
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND_CHILD
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
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

@Tags("test", "unitTest")
@DisplayName("StartProfileInterviewApplicationService 단위테스트")
class StartProfileInterviewApplicationServiceTest : FunSpec({

    val childAccessPort = mockk<ChildAccessPort>()
    val childConsentAccessPort = mockk<ChildConsentAccessPort>()
    val loadProfileInterviewPort = mockk<LoadProfileInterviewPort>()
    val commandProfileInterviewPort = mockk<CommandProfileInterviewPort>()
    val loadInterviewMessagePort = mockk<LoadInterviewMessagePort>()
    val commandInterviewMessagePort = mockk<CommandInterviewMessagePort>()
    val interviewReplyPort = mockk<InterviewReplyPort>()
    val interviewTurnSettingsPort = mockk<InterviewTurnSettingsPort>()
    val ttsPort = mockk<TtsPort>()
    val fixedInstant = LocalDateTime.of(2026, 8, 17, 10, 0).toInstant(ZoneOffset.UTC)
    val clock = Clock.fixed(fixedInstant, ZoneId.of("UTC"))
    val service = StartProfileInterviewApplicationService(
        childAccessPort = childAccessPort,
        childConsentAccessPort = childConsentAccessPort,
        loadProfileInterviewPort = loadProfileInterviewPort,
        commandProfileInterviewPort = commandProfileInterviewPort,
        loadInterviewMessagePort = loadInterviewMessagePort,
        commandInterviewMessagePort = commandInterviewMessagePort,
        interviewReplyPort = interviewReplyPort,
        interviewTurnSettingsPort = interviewTurnSettingsPort,
        ttsPort = ttsPort,
        clock = clock,
    )

    beforeEach {
        clearAllMocks()
        every { interviewTurnSettingsPort.load() } returns emptyMap()
    }

    val childIdStr = "child-uuid-1"
    val guardianIdStr = "guardian-uuid-1"
    val childId = ChildId(childIdStr)
    val guardianId = UserId(guardianIdStr)

    fun command(): StartProfileInterviewCommand =
        StartProfileInterviewCommand(guardianId = guardianIdStr, childId = childIdStr)

    context("성공 - 신규 시작") {
        test("인터뷰를 만들고 아이 이름으로 개인화한 첫 큐미 질문을 저장·합성해 반환한다") {
            val interviewSlot = slot<ProfileInterview>()
            val messageSlot = slot<InterviewMessage>()
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every { childConsentAccessPort.hasActiveConsent(childId) } returns true
            every { loadProfileInterviewPort.findInProgressByChild(childId) } returns null
            every { commandProfileInterviewPort.save(capture(interviewSlot)) } answers { interviewSlot.captured }
            every { childAccessPort.findChildName(childId) } returns "민서"
            every { commandInterviewMessagePort.save(capture(messageSlot)) } answers { messageSlot.captured }
            every { ttsPort.synthesize(any(), QUMI_VOICE_PROFILE) } returns "audio://qumi-1"

            val result = service.execute(command())

            result.created shouldBe true
            result.status shouldBe "IN_PROGRESS"
            result.stage shouldBe InterviewStage.FREE_TALK.name
            result.qumiText shouldBe "안녕 민서야! 나는 큐미야! 오늘은 민서랑 재미있는 이야기를 만들어 볼 거야. 민서는 어떤 이야기를 좋아해?"
            result.qumiAudio shouldBe "audio://qumi-1"
            messageSlot.captured.turnOrder shouldBe 1L
            messageSlot.captured.interviewId shouldBe interviewSlot.captured.interviewId
            verify(exactly = 1) { ttsPort.synthesize(result.qumiText, QUMI_VOICE_PROFILE) }
        }

        test("받침 있는 이름은 호격·조사가 맞게 붙는다 (하은아, 하은이랑, 하은이는)") {
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every { childConsentAccessPort.hasActiveConsent(childId) } returns true
            every { loadProfileInterviewPort.findInProgressByChild(childId) } returns null
            every { commandProfileInterviewPort.save(any()) } answers { firstArg() }
            every { childAccessPort.findChildName(childId) } returns "하은"
            every { commandInterviewMessagePort.save(any()) } answers { firstArg() }
            every { ttsPort.synthesize(any(), any()) } returns "audio://x"

            val result = service.execute(command())

            result.qumiText shouldBe "안녕 하은아! 나는 큐미야! 오늘은 하은이랑 재미있는 이야기를 만들어 볼 거야. 하은이는 어떤 이야기를 좋아해?"
        }
    }

    context("성공 - 재진입") {
        test("진행 중 인터뷰가 있으면 새로 만들지 않고 마지막 큐미 질문을 반환한다") {
            val existing = ProfileInterview.start(childId, LocalDateTime.of(2026, 8, 16, 9, 0))
            val latestQumi = InterviewMessage.qumiLine(
                interviewId = existing.interviewId,
                turnOrder = 5L,
                text = "토끼가 왜 좋아?",
                at = LocalDateTime.of(2026, 8, 16, 9, 5),
            )
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every { childConsentAccessPort.hasActiveConsent(childId) } returns true
            every { loadProfileInterviewPort.findInProgressByChild(childId) } returns existing
            every { loadInterviewMessagePort.findLatestQumiMessage(existing.interviewId) } returns latestQumi
            every { ttsPort.synthesize("토끼가 왜 좋아?", QUMI_VOICE_PROFILE) } returns "audio://qumi-5"

            val result = service.execute(command())

            result.created shouldBe false
            result.interviewId shouldBe existing.interviewId.value
            result.qumiText shouldBe "토끼가 왜 좋아?"
            result.qumiAudio shouldBe "audio://qumi-5"
            verify(exactly = 0) { commandProfileInterviewPort.save(any()) }
            verify(exactly = 0) { commandInterviewMessagePort.save(any()) }
        }
    }

    context("예외케이스") {
        test("아이 소유자가 다르면 NOT_FOUND_CHILD를 던지고 아무것도 만들지 않는다") {
            every { childAccessPort.findGuardianId(childId) } returns UserId("other-guardian")

            val exception = shouldThrow<NotFoundException> { service.execute(command()) }

            exception.exceptionResponseCode shouldBe NOT_FOUND_CHILD
            verify(exactly = 0) { commandProfileInterviewPort.save(any()) }
        }

        test("활성 동의가 없으면 CONSENT_REQUIRED를 던지고 아무것도 만들지 않는다") {
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every { childConsentAccessPort.hasActiveConsent(childId) } returns false

            val exception = shouldThrow<BusinessRuleViolationException> { service.execute(command()) }

            exception.exceptionResponseCode shouldBe CONSENT_REQUIRED
            verify(exactly = 0) { commandProfileInterviewPort.save(any()) }
            verify(exactly = 0) { commandInterviewMessagePort.save(any()) }
        }
    }
})
