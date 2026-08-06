package com.krince.reminisce.application.service.postactivity

import com.krince.reminisce.application.port.access.child.ChildAccessPort
import com.krince.reminisce.application.port.access.story.StoryAccessPort
import com.krince.reminisce.application.port.`in`.postactivity.command.SubmitCardOrderCommand
import com.krince.reminisce.application.port.out.postactivityresult.CommandPostActivityResultPort
import com.krince.reminisce.application.port.out.postactivityresult.LoadPostActivityResultPort
import com.krince.reminisce.application.port.out.speakingsession.LoadSpeakingSessionPort
import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.postactivityresult.PostActivityResult
import com.krince.reminisce.domain.model.postactivityresult.vo.PostActivityResultId
import com.krince.reminisce.domain.model.speakingsession.SpeakingSession
import com.krince.reminisce.domain.model.speakingsession.vo.SessionStatus
import com.krince.reminisce.domain.model.speakingsession.vo.SpeakingSessionId
import com.krince.reminisce.domain.model.story.vo.PostActivityConfig
import com.krince.reminisce.domain.model.story.vo.StoryId
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
import java.time.LocalDateTime

@Tags("test", "unitTest")
@DisplayName("SubmitCardOrderApplicationService 단위테스트")
class SubmitCardOrderApplicationServiceTest : FunSpec({

    val loadSpeakingSessionPort = mockk<LoadSpeakingSessionPort>()
    val childAccessPort = mockk<ChildAccessPort>()
    val storyAccessPort = mockk<StoryAccessPort>()
    val loadPostActivityResultPort = mockk<LoadPostActivityResultPort>()
    val commandPostActivityResultPort = mockk<CommandPostActivityResultPort>()
    val service = SubmitCardOrderApplicationService(
        loadSpeakingSessionPort = loadSpeakingSessionPort,
        childAccessPort = childAccessPort,
        storyAccessPort = storyAccessPort,
        loadPostActivityResultPort = loadPostActivityResultPort,
        commandPostActivityResultPort = commandPostActivityResultPort,
    )

    beforeEach { clearAllMocks() }

    val sessionIdStr = "session-uuid-1"
    val guardianIdStr = "guardian-uuid-1"
    val guardianId = UserId(guardianIdStr)
    val otherGuardianId = UserId("guardian-uuid-2")
    val childId = ChildId("child-uuid-1")
    val storyId = StoryId("story-uuid-1")

    val cardA = PostActivityConfig.Card(id = "card-a", text = "A 장면", correctOrder = 1)
    val cardB = PostActivityConfig.Card(id = "card-b", text = "B 장면", correctOrder = 2)
    val cardC = PostActivityConfig.Card(id = "card-c", text = "C 장면", correctOrder = 3)
    val correctOrder = listOf("card-a", "card-b", "card-c")
    val keywords = listOf("방귀", "며느리", "시아버지")
    val config = PostActivityConfig(cards = listOf(cardC, cardA, cardB), retellingKeywords = keywords)

    fun command(order: List<String> = correctOrder): SubmitCardOrderCommand =
        SubmitCardOrderCommand(sessionId = sessionIdStr, guardianId = guardianIdStr, order = order)

    fun session(status: SessionStatus = SessionStatus.POST_ACTIVITY): SpeakingSession = SpeakingSession(
        sessionId = SpeakingSessionId(sessionIdStr),
        childId = childId,
        storyId = storyId,
        status = status,
        startedAt = LocalDateTime.now().minusMinutes(10),
        lastActivityAt = LocalDateTime.now().minusMinutes(1),
    )

    fun savedResult(
        submittedOrder: List<String> = correctOrder,
        isOrderCorrect: Boolean = true,
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
        }

        test("타 보호자의 아이 세션이면 NOT_FOUND로 은닉하고 저장하지 않는다") {
            every { loadSpeakingSessionPort.findById(SpeakingSessionId(sessionIdStr)) } returns session()
            every { childAccessPort.findGuardianId(childId) } returns otherGuardianId

            val exception = shouldThrow<NotFoundException> { service.execute(command()) }

            exception.exceptionResponseCode shouldBe NOT_FOUND
            verify(exactly = 0) { commandPostActivityResultPort.save(any()) }
        }

        test("status가 IN_PROGRESS이면 BUSINESS_RULE_VIOLATION을 던지고 정답 계산·저장을 하지 않는다") {
            every {
                loadSpeakingSessionPort.findById(SpeakingSessionId(sessionIdStr))
            } returns session(SessionStatus.IN_PROGRESS)
            every { childAccessPort.findGuardianId(childId) } returns guardianId

            val exception = shouldThrow<BusinessRuleViolationException> { service.execute(command()) }

            exception.exceptionResponseCode shouldBe BUSINESS_RULE_VIOLATION
            verify(exactly = 0) { storyAccessPort.findPostActivityConfig(any()) }
            verify(exactly = 0) { commandPostActivityResultPort.save(any()) }
        }
    }

    context("성공 - 첫 제출") {
        test("정답 순서를 제출하면 isOrderCorrect=true이고 retellingKeywords를 포함한 결과를 반환한다") {
            every { loadSpeakingSessionPort.findById(SpeakingSessionId(sessionIdStr)) } returns session()
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every { storyAccessPort.findPostActivityConfig(storyId) } returns config
            every { loadPostActivityResultPort.findBySession(SpeakingSessionId(sessionIdStr)) } returns null
            val savedSlot = slot<PostActivityResult>()
            every { commandPostActivityResultPort.save(capture(savedSlot)) } answers {
                savedResult(submittedOrder = correctOrder, isOrderCorrect = true, attemptCount = 1)
            }

            val result = service.execute(command(correctOrder))

            result.isOrderCorrect shouldBe true
            result.attemptCount shouldBe 1
            result.retellingKeywords shouldBe keywords
            savedSlot.captured.submittedOrder shouldBe correctOrder
            savedSlot.captured.isOrderCorrect shouldBe true
            savedSlot.captured.attemptCount shouldBe 1
        }

        test("오답 순서를 제출하면 isOrderCorrect=false이고 retellingKeywords가 빈 리스트이다") {
            val wrongOrder = listOf("card-c", "card-a", "card-b")
            every { loadSpeakingSessionPort.findById(SpeakingSessionId(sessionIdStr)) } returns session()
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every { storyAccessPort.findPostActivityConfig(storyId) } returns config
            every { loadPostActivityResultPort.findBySession(SpeakingSessionId(sessionIdStr)) } returns null
            val savedSlot = slot<PostActivityResult>()
            every { commandPostActivityResultPort.save(capture(savedSlot)) } answers {
                savedResult(submittedOrder = wrongOrder, isOrderCorrect = false, attemptCount = 1)
            }

            val result = service.execute(command(wrongOrder))

            result.isOrderCorrect shouldBe false
            result.retellingKeywords shouldBe emptyList()
            savedSlot.captured.isOrderCorrect shouldBe false
        }
    }

    context("성공 - 재제출") {
        test("기존 결과가 있으면 resubmit으로 submittedOrder·isOrderCorrect를 갱신하고 attemptCount가 1 증가한다") {
            val wrongOrder = listOf("card-c", "card-b", "card-a")
            val existingResult = savedResult(submittedOrder = wrongOrder, isOrderCorrect = false, attemptCount = 1)
            every { loadSpeakingSessionPort.findById(SpeakingSessionId(sessionIdStr)) } returns session()
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every { storyAccessPort.findPostActivityConfig(storyId) } returns config
            every { loadPostActivityResultPort.findBySession(SpeakingSessionId(sessionIdStr)) } returns existingResult
            val savedSlot = slot<PostActivityResult>()
            every { commandPostActivityResultPort.save(capture(savedSlot)) } answers {
                savedResult(submittedOrder = correctOrder, isOrderCorrect = true, attemptCount = 2)
            }

            val result = service.execute(command(correctOrder))

            result.isOrderCorrect shouldBe true
            result.attemptCount shouldBe 2
            result.retellingKeywords shouldBe keywords
            savedSlot.captured.submittedOrder shouldBe correctOrder
            savedSlot.captured.isOrderCorrect shouldBe true
            savedSlot.captured.attemptCount shouldBe 2
            verify(exactly = 1) { commandPostActivityResultPort.save(any()) }
        }
    }
})
