package com.krince.reminisce.application.service.report

import com.krince.reminisce.application.port.access.child.ChildAccessPort
import com.krince.reminisce.application.port.`in`.report.command.GetLatestSessionReportCommand
import com.krince.reminisce.application.port.`in`.report.command.GetSessionReportCommand
import com.krince.reminisce.application.port.`in`.report.result.SessionReportResult
import com.krince.reminisce.application.port.`in`.report.usecase.GetSessionReportUseCase
import com.krince.reminisce.application.port.out.speakingsession.LoadSpeakingSessionPort
import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.speakingsession.SpeakingSession
import com.krince.reminisce.domain.model.story.vo.StoryId
import com.krince.reminisce.domain.model.user.vo.UserId
import com.krince.reminisce.shared.exception.NotFoundException
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND
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
import java.time.LocalDateTime

@Tags("test", "unitTest")
@DisplayName("GetLatestSessionReportApplicationService 단위테스트")
class GetLatestSessionReportApplicationServiceTest : FunSpec({

    val childAccessPort = mockk<ChildAccessPort>()
    val loadSpeakingSessionPort = mockk<LoadSpeakingSessionPort>()
    val getSessionReportUseCase = mockk<GetSessionReportUseCase>()
    val service = GetLatestSessionReportApplicationService(
        childAccessPort = childAccessPort,
        loadSpeakingSessionPort = loadSpeakingSessionPort,
        getSessionReportUseCase = getSessionReportUseCase,
    )

    beforeEach { clearAllMocks() }

    val childIdStr = "child-uuid-1"
    val guardianIdStr = "guardian-uuid-1"
    val childId = ChildId(childIdStr)
    val guardianId = UserId(guardianIdStr)
    val startedAt = LocalDateTime.of(2026, 8, 17, 10, 0)

    fun command(): GetLatestSessionReportCommand =
        GetLatestSessionReportCommand(childId = childIdStr, guardianId = guardianIdStr)

    fun completedSession(): SpeakingSession = SpeakingSession.start(childId, StoryId("story-1"), startedAt)
        .enterPostActivity(startedAt.plusMinutes(5))
        .complete(startedAt.plusMinutes(10))

    context("성공") {
        test("가장 최근 완료 세션을 찾아 그 세션 리포트 조회에 위임하고 세션 식별자를 함께 반환한다") {
            val latest = completedSession()
            val report = mockk<SessionReportResult>()
            val delegatedCommand = slot<GetSessionReportCommand>()
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every { loadSpeakingSessionPort.findLatestCompletedByChild(childId) } returns latest
            every { getSessionReportUseCase.execute(capture(delegatedCommand)) } returns report

            val result = service.execute(command())

            result.sessionId shouldBe latest.sessionId.value
            result.report shouldBe report
            delegatedCommand.captured.sessionId shouldBe latest.sessionId.value
            delegatedCommand.captured.guardianId shouldBe guardianIdStr
        }
    }

    context("예외케이스") {
        test("완료된 세션이 없으면 NOT_FOUND를 던지고 리포트 조회에 위임하지 않는다") {
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every { loadSpeakingSessionPort.findLatestCompletedByChild(childId) } returns null

            val exception = shouldThrow<NotFoundException> { service.execute(command()) }

            exception.exceptionResponseCode shouldBe NOT_FOUND
            verify(exactly = 0) { getSessionReportUseCase.execute(any()) }
        }

        test("남의 아이면 NOT_FOUND_CHILD로 은닉한다") {
            every { childAccessPort.findGuardianId(childId) } returns UserId("other-guardian")

            val exception = shouldThrow<NotFoundException> { service.execute(command()) }

            exception.exceptionResponseCode shouldBe NOT_FOUND_CHILD
            verify(exactly = 0) { loadSpeakingSessionPort.findLatestCompletedByChild(any()) }
        }
    }
})
