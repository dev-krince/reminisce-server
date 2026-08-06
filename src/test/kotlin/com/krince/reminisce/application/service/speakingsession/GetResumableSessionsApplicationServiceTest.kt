package com.krince.reminisce.application.service.speakingsession

import com.krince.reminisce.application.port.access.child.ChildAccessPort
import com.krince.reminisce.application.port.`in`.speakingsession.command.GetResumableSessionsCommand
import com.krince.reminisce.application.port.out.speakingsession.LoadSpeakingSessionPort
import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.speakingsession.SpeakingSession
import com.krince.reminisce.domain.model.speakingsession.vo.SessionStatus
import com.krince.reminisce.domain.model.speakingsession.vo.SpeakingSessionId
import com.krince.reminisce.domain.model.story.vo.StoryId
import com.krince.reminisce.domain.model.user.vo.UserId
import com.krince.reminisce.shared.exception.NotFoundException
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDateTime

@Tags("test", "unitTest")
@DisplayName("GetResumableSessionsApplicationService 단위테스트")
class GetResumableSessionsApplicationServiceTest : FunSpec({

    val loadSpeakingSessionPort = mockk<LoadSpeakingSessionPort>()
    val childAccessPort = mockk<ChildAccessPort>()
    val service = GetResumableSessionsApplicationService(
        loadSpeakingSessionPort = loadSpeakingSessionPort,
        childAccessPort = childAccessPort,
    )

    beforeEach { clearAllMocks() }

    val childIdStr = "child-uuid-1"
    val guardianIdStr = "guardian-uuid-1"
    val childId = ChildId(childIdStr)
    val guardianId = UserId(guardianIdStr)
    val otherGuardianId = UserId("guardian-uuid-2")
    val storyId = StoryId("story-uuid-1")
    val recentAt = LocalDateTime.now().minusMinutes(1)
    val olderAt = LocalDateTime.now().minusMinutes(3)

    fun command(): GetResumableSessionsCommand =
        GetResumableSessionsCommand(childId = childIdStr, guardianId = guardianIdStr)

    fun session(sessionId: String, lastActivityAt: LocalDateTime): SpeakingSession = SpeakingSession(
        sessionId = SpeakingSessionId(sessionId),
        childId = childId,
        storyId = storyId,
        status = SessionStatus.IN_PROGRESS,
        currentSceneId = null,
        startedAt = LocalDateTime.now().minusMinutes(10),
        lastActivityAt = lastActivityAt,
    )

    context("소유권 실패") {
        test("findGuardianId가 null이면 NotFoundException(NOT_FOUND)을 던진다") {
            every { childAccessPort.findGuardianId(childId) } returns null

            val exception = shouldThrow<NotFoundException> { service.execute(command()) }

            exception.exceptionResponseCode shouldBe NOT_FOUND
        }

        test("findGuardianId가 다른 보호자면 NotFoundException(NOT_FOUND)을 던진다") {
            every { childAccessPort.findGuardianId(childId) } returns otherGuardianId

            val exception = shouldThrow<NotFoundException> { service.execute(command()) }

            exception.exceptionResponseCode shouldBe NOT_FOUND
        }
    }

    context("성공") {
        test("in_progress 세션 2건을 lastActivityAt 내림차순으로 요약 매핑해 반환한다") {
            val recentSession = session("session-recent", recentAt)
            val olderSession = session("session-older", olderAt)
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every { loadSpeakingSessionPort.findInProgressByChild(childId) } returns listOf(recentSession, olderSession)

            val results = service.execute(command())

            results.size shouldBe 2
            results[0].sessionId shouldBe "session-recent"
            results[0].storyId shouldBe storyId.value
            results[0].status shouldBe SessionStatus.IN_PROGRESS.name
            results[0].currentSceneId shouldBe null
            results[0].startedAt shouldBe recentSession.startedAt
            results[0].lastActivityAt shouldBe recentAt
            results[1].sessionId shouldBe "session-older"
            results[1].lastActivityAt shouldBe olderAt
        }
    }
})
