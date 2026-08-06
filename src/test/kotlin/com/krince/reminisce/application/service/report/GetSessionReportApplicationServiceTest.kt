package com.krince.reminisce.application.service.report

import com.krince.reminisce.application.port.access.child.ChildAccessPort
import com.krince.reminisce.application.port.`in`.report.command.GetSessionReportCommand
import com.krince.reminisce.application.port.out.message.LoadMessagePort
import com.krince.reminisce.application.port.out.report.CommandReportPort
import com.krince.reminisce.application.port.out.report.LoadReportPort
import com.krince.reminisce.application.port.out.report.ReportSummaryPort
import com.krince.reminisce.application.port.out.speakingsession.LoadSpeakingSessionPort
import com.krince.reminisce.application.port.out.utteranceanalysis.LoadUtteranceAnalysisPort
import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.message.vo.MessageId
import com.krince.reminisce.domain.model.report.CompetencyAnalysis
import com.krince.reminisce.domain.model.report.HomeConversationGuide
import com.krince.reminisce.domain.model.report.Report
import com.krince.reminisce.domain.model.report.RepresentativeUtterance
import com.krince.reminisce.domain.model.report.vo.ReportId
import com.krince.reminisce.domain.model.speakingsession.SpeakingSession
import com.krince.reminisce.domain.model.speakingsession.vo.SessionStatus
import com.krince.reminisce.domain.model.speakingsession.vo.SpeakingSessionId
import com.krince.reminisce.domain.model.story.vo.StoryId
import com.krince.reminisce.domain.model.story.vo.ThinkingElement
import com.krince.reminisce.domain.model.user.vo.UserId
import com.krince.reminisce.domain.model.utteranceanalysis.DetectedElement
import com.krince.reminisce.domain.model.utteranceanalysis.UtteranceAnalysis
import com.krince.reminisce.domain.model.utteranceanalysis.vo.AnalysisId
import com.krince.reminisce.domain.model.utteranceanalysis.vo.ChildIntent
import com.krince.reminisce.domain.model.utteranceanalysis.vo.UtteranceValidity
import com.krince.reminisce.shared.exception.BusinessRuleViolationException
import com.krince.reminisce.shared.exception.NotFoundException
import com.krince.reminisce.shared.response.ExceptionResponseCode.BUSINESS_RULE_VIOLATION
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND
import com.krince.reminisce.testutil.fixture.TestGuardianReportAreasFixture
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
@DisplayName("GetSessionReportApplicationService 단위테스트")
class GetSessionReportApplicationServiceTest : FunSpec({

    val loadSpeakingSessionPort = mockk<LoadSpeakingSessionPort>()
    val childAccessPort = mockk<ChildAccessPort>()
    val loadReportPort = mockk<LoadReportPort>()
    val commandReportPort = mockk<CommandReportPort>()
    val loadMessagePort = mockk<LoadMessagePort>()
    val loadUtteranceAnalysisPort = mockk<LoadUtteranceAnalysisPort>()
    val reportSummaryPort = mockk<ReportSummaryPort>()
    val fixedInstant = Instant.parse("2026-01-09T14:30:25Z")
    val clock = Clock.fixed(fixedInstant, ZoneOffset.UTC)
    val service = GetSessionReportApplicationService(
        loadSpeakingSessionPort = loadSpeakingSessionPort,
        childAccessPort = childAccessPort,
        loadReportPort = loadReportPort,
        commandReportPort = commandReportPort,
        loadMessagePort = loadMessagePort,
        loadUtteranceAnalysisPort = loadUtteranceAnalysisPort,
        reportSummaryPort = reportSummaryPort,
        clock = clock,
    )

    beforeEach { clearAllMocks() }

    val sessionIdStr = "session-uuid-1"
    val guardianIdStr = "guardian-uuid-1"
    val guardianId = UserId(guardianIdStr)
    val otherGuardianId = UserId("guardian-uuid-2")
    val childId = ChildId("child-uuid-1")
    val storyId = StoryId("story-uuid-1")
    val sessionId = SpeakingSessionId(sessionIdStr)
    val stubSummary = "요약-스텁"

    fun command(): GetSessionReportCommand =
        GetSessionReportCommand(sessionId = sessionIdStr, guardianId = guardianIdStr)

    fun session(status: SessionStatus = SessionStatus.COMPLETED): SpeakingSession = SpeakingSession(
        sessionId = sessionId,
        childId = childId,
        storyId = storyId,
        status = status,
        startedAt = LocalDateTime.now().minusMinutes(30),
        lastActivityAt = LocalDateTime.now().minusMinutes(1),
    )

    fun analysis(messageId: String, vararg types: ThinkingElement): UtteranceAnalysis = UtteranceAnalysis(
        analysisId = AnalysisId("analysis-$messageId"),
        messageId = MessageId(messageId),
        childIntent = ChildIntent.OPINION,
        mainPoint = "핵심",
        detectedElements = types.map { DetectedElement(type = it, evidence = "근거-${it.name}") },
        validity = UtteranceValidity.VALID,
    )

    fun competencyAnalysis(): CompetencyAnalysis = TestGuardianReportAreasFixture.competencyAnalysis()

    context("완료 세션 - 기존 리포트 없음") {
        test("세션 아이 메시지 분석을 집계해 strengths 합집합·nextFocus 상보·스텁 summary로 리포트를 저장하고 반환한다") {
            val messageIds = listOf(MessageId("msg-1"), MessageId("msg-2"))
            every { loadSpeakingSessionPort.findById(sessionId) } returns session()
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every { loadReportPort.findBySession(sessionId) } returns null
            every { loadMessagePort.findChildMessageIdsBySession(sessionId) } returns messageIds
            every { loadUtteranceAnalysisPort.findByMessageIds(messageIds) } returns listOf(
                analysis("msg-1", ThinkingElement.EMOTION, ThinkingElement.PERSPECTIVE),
                analysis("msg-2", ThinkingElement.EMOTION, ThinkingElement.DECISION),
            )
            val expectedStrengths = listOf(ThinkingElement.EMOTION, ThinkingElement.PERSPECTIVE, ThinkingElement.DECISION)
            val expectedNextFocus = ThinkingElement.entries.filterNot { it in expectedStrengths }
            every { reportSummaryPort.generate(any(), any()) } returns stubSummary
            val savedSlot = slot<Report>()
            every { commandReportPort.save(capture(savedSlot)) } answers { savedSlot.captured }

            val result = service.execute(command())

            result.strengths shouldBe expectedStrengths
            result.nextFocus shouldBe expectedNextFocus
            result.summary shouldBe stubSummary
            result.createdAt shouldBe LocalDateTime.ofInstant(fixedInstant, ZoneOffset.UTC)
            savedSlot.captured.sessionId shouldBe sessionId
            savedSlot.captured.strengths shouldBe expectedStrengths
            savedSlot.captured.nextFocus shouldBe expectedNextFocus
            (savedSlot.captured.strengths + savedSlot.captured.nextFocus).toSet() shouldBe ThinkingElement.entries.toSet()
            savedSlot.captured.strengths.intersect(savedSlot.captured.nextFocus.toSet()) shouldBe emptySet()
            verify(exactly = 1) { commandReportPort.save(any()) }
        }

        test("확인된 사고 요소가 없으면 strengths는 비고 nextFocus는 캐논 8종 전체다") {
            every { loadSpeakingSessionPort.findById(sessionId) } returns session()
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every { loadReportPort.findBySession(sessionId) } returns null
            every { loadMessagePort.findChildMessageIdsBySession(sessionId) } returns emptyList()
            every { loadUtteranceAnalysisPort.findByMessageIds(emptyList()) } returns emptyList()
            every { reportSummaryPort.generate(any(), any()) } returns stubSummary
            val savedSlot = slot<Report>()
            every { commandReportPort.save(capture(savedSlot)) } answers { savedSlot.captured }

            val result = service.execute(command())

            result.strengths shouldBe emptyList()
            result.nextFocus shouldBe ThinkingElement.entries.toList()
        }
    }

    context("완료 세션 - 기존 리포트 존재") {
        test("기존 리포트가 있으면 그대로 반환하고 집계·저장을 하지 않는다") {
            val existing = Report(
                reportId = ReportId("report-uuid-1"),
                sessionId = sessionId,
                summary = "기존-요약",
                strengths = listOf(ThinkingElement.EMOTION),
                nextFocus = ThinkingElement.entries.filterNot { it == ThinkingElement.EMOTION },
                competencyAnalysis = competencyAnalysis(),
                representativeUtterance = RepresentativeUtterance(text = "기존-대표", reason = "기존-이유"),
                homeConversationGuide = HomeConversationGuide(
                    storyThemeQuestions = listOf("기존-주제-질문"),
                    dailyLifeQuestions = listOf("기존-일상-질문"),
                ),
                createdAt = LocalDateTime.of(2026, 1, 1, 9, 0, 0),
            )
            every { loadSpeakingSessionPort.findById(sessionId) } returns session()
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every { loadReportPort.findBySession(sessionId) } returns existing

            val result = service.execute(command())

            result.summary shouldBe "기존-요약"
            result.strengths shouldBe listOf(ThinkingElement.EMOTION)
            result.createdAt shouldBe existing.createdAt
            verify(exactly = 0) { loadMessagePort.findChildMessageIdsBySession(any()) }
            verify(exactly = 0) { loadUtteranceAnalysisPort.findByMessageIds(any()) }
            verify(exactly = 0) { commandReportPort.save(any()) }
        }
    }

    context("3영역 파생") {
        fun stubGenerateSession() {
            every { loadSpeakingSessionPort.findById(sessionId) } returns session()
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every { loadReportPort.findBySession(sessionId) } returns null
            every { reportSummaryPort.generate(any(), any()) } returns stubSummary
        }

        test("PERSPECTIVE·EMPATHY 검출은 관점·공감을 잘한 점으로, REQUEST 미검출은 상호작용을 보완점으로 채운다") {
            stubGenerateSession()
            val messageIds = listOf(MessageId("msg-1"))
            every { loadMessagePort.findChildMessageIdsBySession(sessionId) } returns messageIds
            every { loadUtteranceAnalysisPort.findByMessageIds(messageIds) } returns listOf(
                analysis("msg-1", ThinkingElement.PERSPECTIVE, ThinkingElement.EMPATHY),
            )
            val savedSlot = slot<Report>()
            every { commandReportPort.save(capture(savedSlot)) } answers { savedSlot.captured }

            val result = service.execute(command())

            val perspectiveEmpathy = result.competencyAnalysis.perspectiveEmpathy
            perspectiveEmpathy.label shouldBe "관점·공감"
            perspectiveEmpathy.evidenceUtterance shouldBe "근거-PERSPECTIVE"
            val interaction = result.competencyAnalysis.interaction
            interaction.label shouldBe "상호작용"
            interaction.evidenceUtterance shouldBe null
            perspectiveEmpathy.strength shouldBe savedSlot.captured.competencyAnalysis.perspectiveEmpathy.strength
        }

        test("대표 발화는 detectedElements가 가장 많은 아이 발화 evidence로 선정되고 가정 연계 질문은 비어있지 않다") {
            stubGenerateSession()
            val messageIds = listOf(MessageId("msg-1"), MessageId("msg-2"))
            every { loadMessagePort.findChildMessageIdsBySession(sessionId) } returns messageIds
            every { loadUtteranceAnalysisPort.findByMessageIds(messageIds) } returns listOf(
                analysis("msg-1", ThinkingElement.EMOTION),
                analysis("msg-2", ThinkingElement.PERSPECTIVE, ThinkingElement.EMPATHY, ThinkingElement.DECISION),
            )
            val savedSlot = slot<Report>()
            every { commandReportPort.save(capture(savedSlot)) } answers { savedSlot.captured }

            val result = service.execute(command())

            result.representativeUtterance.text shouldBe "근거-PERSPECTIVE"
            result.representativeUtterance.reason.isNotBlank() shouldBe true
            result.homeConversationGuide.storyThemeQuestions.isNotEmpty() shouldBe true
            result.homeConversationGuide.dailyLifeQuestions.isNotEmpty() shouldBe true
        }

        test("발화·분석이 없어도 예외 없이 안전 기본값으로 3영역을 채운다") {
            stubGenerateSession()
            every { loadMessagePort.findChildMessageIdsBySession(sessionId) } returns emptyList()
            every { loadUtteranceAnalysisPort.findByMessageIds(emptyList()) } returns emptyList()
            val savedSlot = slot<Report>()
            every { commandReportPort.save(capture(savedSlot)) } answers { savedSlot.captured }

            val result = service.execute(command())

            result.representativeUtterance.text shouldBe null
            result.competencyAnalysis.perspectiveEmpathy.evidenceUtterance shouldBe null
            result.homeConversationGuide.storyThemeQuestions.isNotEmpty() shouldBe true
            result.homeConversationGuide.dailyLifeQuestions.isNotEmpty() shouldBe true
        }
    }

    context("게이트 실패") {
        test("status가 COMPLETED가 아니면 BUSINESS_RULE_VIOLATION을 던지고 집계·저장을 하지 않는다") {
            every { loadSpeakingSessionPort.findById(sessionId) } returns session(SessionStatus.IN_PROGRESS)
            every { childAccessPort.findGuardianId(childId) } returns guardianId

            val exception = shouldThrow<BusinessRuleViolationException> { service.execute(command()) }

            exception.exceptionResponseCode shouldBe BUSINESS_RULE_VIOLATION
            verify(exactly = 0) { loadReportPort.findBySession(any()) }
            verify(exactly = 0) { loadMessagePort.findChildMessageIdsBySession(any()) }
            verify(exactly = 0) { commandReportPort.save(any()) }
        }

        test("세션이 없으면 NOT_FOUND를 던지고 저장하지 않는다") {
            every { loadSpeakingSessionPort.findById(sessionId) } returns null

            val exception = shouldThrow<NotFoundException> { service.execute(command()) }

            exception.exceptionResponseCode shouldBe NOT_FOUND
            verify(exactly = 0) { commandReportPort.save(any()) }
        }

        test("타 보호자의 아이 세션이면 NOT_FOUND로 은닉하고 저장하지 않는다") {
            every { loadSpeakingSessionPort.findById(sessionId) } returns session()
            every { childAccessPort.findGuardianId(childId) } returns otherGuardianId

            val exception = shouldThrow<NotFoundException> { service.execute(command()) }

            exception.exceptionResponseCode shouldBe NOT_FOUND
            verify(exactly = 0) { commandReportPort.save(any()) }
        }
    }
})
