package com.krince.reminisce.application.service.profileinterview

import com.krince.reminisce.application.port.access.child.ChildAccessPort
import com.krince.reminisce.application.port.`in`.profileinterview.command.SubmitInterviewUtteranceCommand
import com.krince.reminisce.application.port.out.profileinterview.CommandInterviewMessagePort
import com.krince.reminisce.application.port.out.profileinterview.CommandProfileInterviewPort
import com.krince.reminisce.application.port.out.profileinterview.InterviewReplyContext
import com.krince.reminisce.application.port.out.profileinterview.InterviewReplyPort
import com.krince.reminisce.application.port.out.profileinterview.InterviewTurnSettingsPort
import com.krince.reminisce.application.port.out.profileinterview.LoadInterviewMessagePort
import com.krince.reminisce.application.port.out.profileinterview.LoadProfileInterviewPort
import com.krince.reminisce.application.port.out.tts.QUMI_VOICE_PROFILE
import com.krince.reminisce.application.port.out.tts.TtsPort
import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.profileinterview.InterviewMessage
import com.krince.reminisce.domain.model.profileinterview.ProfileInterview
import com.krince.reminisce.domain.model.profileinterview.vo.InterviewSpeaker
import com.krince.reminisce.domain.model.profileinterview.vo.InterviewStage
import com.krince.reminisce.domain.model.profileinterview.vo.ProfileInterviewId
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
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

@Tags("test", "unitTest")
@DisplayName("SubmitInterviewUtteranceApplicationService 단위테스트")
class SubmitInterviewUtteranceApplicationServiceTest : FunSpec({

    val childAccessPort = mockk<ChildAccessPort>()
    val loadProfileInterviewPort = mockk<LoadProfileInterviewPort>()
    val commandProfileInterviewPort = mockk<CommandProfileInterviewPort>()
    val loadInterviewMessagePort = mockk<LoadInterviewMessagePort>()
    val commandInterviewMessagePort = mockk<CommandInterviewMessagePort>()
    val interviewReplyPort = mockk<InterviewReplyPort>()
    val interviewTurnSettingsPort = mockk<InterviewTurnSettingsPort>()
    val ttsPort = mockk<TtsPort>()
    val fixedInstant = LocalDateTime.of(2026, 8, 17, 11, 0).toInstant(ZoneOffset.UTC)
    val clock = Clock.fixed(fixedInstant, ZoneId.of("UTC"))
    val service = SubmitInterviewUtteranceApplicationService(
        childAccessPort = childAccessPort,
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
    val startedAt = LocalDateTime.of(2026, 8, 17, 10, 0)

    fun inProgressInterview(): ProfileInterview = ProfileInterview.start(childId, startedAt)

    fun command(interviewId: String, text: String = "토끼요."): SubmitInterviewUtteranceCommand =
        SubmitInterviewUtteranceCommand(
            guardianId = guardianIdStr,
            interviewId = interviewId,
            text = text,
            sttRawText = "토끼요",
        )

    fun stubHappyPath(interview: ProfileInterview, messageCount: Long, reply: String) {
        every { loadProfileInterviewPort.findById(interview.interviewId) } returns interview
        every { childAccessPort.findGuardianId(childId) } returns guardianId
        every { loadInterviewMessagePort.findRecentByInterview(interview.interviewId, 6) } returns emptyList()
        every { loadInterviewMessagePort.countByInterview(interview.interviewId) } returns messageCount
        every { commandInterviewMessagePort.save(any()) } answers { firstArg() }
        every { commandProfileInterviewPort.save(any()) } answers { firstArg() }
        every { childAccessPort.findChildName(childId) } returns "민서"
        every { interviewReplyPort.generate(any()) } returns reply
        every { ttsPort.synthesize(reply, QUMI_VOICE_PROFILE) } returns "audio://qumi-next"
    }

    context("성공") {
        test("발화를 저장하고 큐미의 다음 말을 생성·저장해 텍스트·음성으로 반환한다") {
            val interview = inProgressInterview()
            stubHappyPath(interview, messageCount = 1, reply = "토끼를 좋아하는구나! 토끼가 왜 좋아?")
            val messageSlots = mutableListOf<InterviewMessage>()
            every { commandInterviewMessagePort.save(capture(messageSlots)) } answers { messageSlots.last() }

            val result = service.execute(command(interview.interviewId.value))

            result.status shouldBe "IN_PROGRESS"
            result.stage shouldBe InterviewStage.EXPERIENCE.name
            result.qumiText shouldBe "토끼를 좋아하는구나! 토끼가 왜 좋아?"
            result.qumiAudio shouldBe "audio://qumi-next"
            messageSlots.size shouldBe 2
            messageSlots[0].speaker shouldBe InterviewSpeaker.CHILD
            messageSlots[0].turnOrder shouldBe 2L
            messageSlots[0].sttRawText shouldBe "토끼요"
            messageSlots[1].speaker shouldBe InterviewSpeaker.QUMI
            messageSlots[1].turnOrder shouldBe 3L
        }

        test("단계를 마치면 0으로 설정된 단계들을 건너뛰고 다음 활성 단계로 stageOpening 응답을 생성한다") {
            var interview = inProgressInterview()
            interview = interview.advanceOnChildTurn(startedAt.plusMinutes(1))
            interview.currentStage shouldBe InterviewStage.EXPERIENCE
            stubHappyPath(interview, messageCount = 3, reply = "이제 네가 큐미에게 물어볼 차례야!")
            val contextSlot = slot<InterviewReplyContext>()
            every { interviewReplyPort.generate(capture(contextSlot)) } returns "이제 네가 큐미에게 물어볼 차례야!"

            val result = service.execute(command(interview.interviewId.value, text = "동물원에서 봤어요."))

            result.stage shouldBe InterviewStage.CHILD_QUESTION.name
            contextSlot.captured.stage shouldBe InterviewStage.CHILD_QUESTION
            contextSlot.captured.stageOpening shouldBe true
            contextSlot.captured.childUtterance shouldBe "동물원에서 봤어요."
        }

        test("설정으로 단계 턴을 늘리면 그만큼 답하기 전에는 같은 단계에 머문다") {
            val interview = inProgressInterview()
            stubHappyPath(interview, messageCount = 1, reply = "토끼가 왜 좋아?")
            every { interviewTurnSettingsPort.load() } returns mapOf(InterviewStage.FREE_TALK to 2)

            val result = service.execute(command(interview.interviewId.value))

            result.stage shouldBe InterviewStage.FREE_TALK.name
        }

        test("마지막 단계 답을 제출하면 큐미 마무리 인사와 함께 COMPLETED로 끝난다") {
            var interview = inProgressInterview()
            repeat(2) { interview = interview.advanceOnChildTurn(startedAt.plusMinutes(1)) }
            interview.currentStage shouldBe InterviewStage.CHILD_QUESTION
            stubHappyPath(interview, messageCount = 5, reply = "오늘 정말 즐거웠어! 다음에 또 만나자!")
            val savedInterview = slot<ProfileInterview>()
            every { commandProfileInterviewPort.save(capture(savedInterview)) } answers { savedInterview.captured }

            val result = service.execute(command(interview.interviewId.value, text = "숲이 복잡해서요."))

            result.status shouldBe "COMPLETED"
            result.stage shouldBe InterviewStage.CLOSING.name
            result.qumiText shouldBe "오늘 정말 즐거웠어! 다음에 또 만나자!"
            savedInterview.captured.status.name shouldBe "COMPLETED"
        }
    }

    context("예외케이스") {
        test("인터뷰가 없으면 NOT_FOUND를 던지고 아무것도 저장하지 않는다") {
            every { loadProfileInterviewPort.findById(ProfileInterviewId("unknown")) } returns null

            val exception = shouldThrow<NotFoundException> { service.execute(command("unknown")) }

            exception.exceptionResponseCode shouldBe NOT_FOUND
            verify(exactly = 0) { commandInterviewMessagePort.save(any()) }
        }

        test("다른 보호자의 아이 인터뷰면 NOT_FOUND로 은닉한다") {
            val interview = inProgressInterview()
            every { loadProfileInterviewPort.findById(interview.interviewId) } returns interview
            every { childAccessPort.findGuardianId(childId) } returns UserId("other-guardian")

            val exception = shouldThrow<NotFoundException> { service.execute(command(interview.interviewId.value)) }

            exception.exceptionResponseCode shouldBe NOT_FOUND
            verify(exactly = 0) { commandInterviewMessagePort.save(any()) }
        }

        test("완료된 인터뷰에 제출하면 BUSINESS_RULE_VIOLATION을 던진다") {
            val interview = inProgressInterview().complete(startedAt.plusMinutes(30))
            every { loadProfileInterviewPort.findById(interview.interviewId) } returns interview
            every { childAccessPort.findGuardianId(childId) } returns guardianId

            val exception = shouldThrow<BusinessRuleViolationException> {
                service.execute(command(interview.interviewId.value))
            }

            exception.exceptionResponseCode shouldBe BUSINESS_RULE_VIOLATION
            verify(exactly = 0) { commandInterviewMessagePort.save(any()) }
        }
    }
})
