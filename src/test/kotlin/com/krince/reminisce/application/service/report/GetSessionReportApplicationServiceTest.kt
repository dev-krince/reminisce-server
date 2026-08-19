package com.krince.reminisce.application.service.report

import com.krince.reminisce.application.port.access.child.ChildAccessPort
import com.krince.reminisce.application.port.access.story.StoryAccessPort
import com.krince.reminisce.application.port.access.story.StoryReportScene
import com.krince.reminisce.application.port.access.story.StoryReportSnapshot
import com.krince.reminisce.application.port.`in`.report.command.GetSessionReportCommand
import com.krince.reminisce.application.port.out.message.LoadMessagePort
import com.krince.reminisce.application.port.out.postactivityresult.LoadPostActivityResultPort
import com.krince.reminisce.application.port.out.report.CommandReportPort
import com.krince.reminisce.application.port.out.report.LoadReportPort
import com.krince.reminisce.application.port.out.report.ReportAnalysisContext
import com.krince.reminisce.application.port.out.report.ReportAnalysisPort
import com.krince.reminisce.application.port.out.report.ReportAnalysisResult
import com.krince.reminisce.application.port.out.report.RepresentativeSelection
import com.krince.reminisce.application.port.out.speakingsession.LoadSpeakingSessionPort
import com.krince.reminisce.application.port.out.utteranceanalysis.LoadUtteranceAnalysisPort
import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.message.Message
import com.krince.reminisce.domain.model.message.vo.MessageId
import com.krince.reminisce.domain.model.message.vo.SpeakerType
import com.krince.reminisce.domain.model.report.GuideDirection
import com.krince.reminisce.domain.model.report.GuideQuestion
import com.krince.reminisce.domain.model.report.HomeGuide
import com.krince.reminisce.domain.model.report.ParticipationItem
import com.krince.reminisce.domain.model.report.Report
import com.krince.reminisce.domain.model.report.ReportOverall
import com.krince.reminisce.domain.model.report.ReportSpeechAnalysis
import com.krince.reminisce.domain.model.report.RepresentativeUtterance
import com.krince.reminisce.domain.model.report.SceneHighlight
import com.krince.reminisce.domain.model.report.vo.ReportId
import com.krince.reminisce.domain.model.speakingsession.SpeakingSession
import com.krince.reminisce.domain.model.speakingsession.vo.SessionStatus
import com.krince.reminisce.domain.model.speakingsession.vo.SpeakingSessionId
import com.krince.reminisce.domain.model.story.vo.SceneId
import com.krince.reminisce.domain.model.story.vo.SceneType
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
    val storyAccessPort = mockk<StoryAccessPort>()
    val loadReportPort = mockk<LoadReportPort>()
    val commandReportPort = mockk<CommandReportPort>()
    val loadMessagePort = mockk<LoadMessagePort>()
    val loadPostActivityResultPort = mockk<LoadPostActivityResultPort>()
    val loadUtteranceAnalysisPort = mockk<LoadUtteranceAnalysisPort>()
    val reportAnalysisPort = mockk<ReportAnalysisPort>()
    val fixedInstant = Instant.parse("2026-01-09T14:30:25Z")
    val clock = Clock.fixed(fixedInstant, ZoneOffset.UTC)
    val service = GetSessionReportApplicationService(
        loadSpeakingSessionPort = loadSpeakingSessionPort,
        childAccessPort = childAccessPort,
        storyAccessPort = storyAccessPort,
        loadReportPort = loadReportPort,
        commandReportPort = commandReportPort,
        loadMessagePort = loadMessagePort,
        loadPostActivityResultPort = loadPostActivityResultPort,
        loadUtteranceAnalysisPort = loadUtteranceAnalysisPort,
        reportAnalysisPort = reportAnalysisPort,
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
    val childName = "토토"
    val sceneOneId = "scene-1"
    val sceneTwoId = "scene-2"

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

    fun snapshot(): StoryReportSnapshot = StoryReportSnapshot(
        title = "방귀쟁이 며느리",
        scenes = listOf(
            StoryReportScene(
                sceneId = sceneOneId,
                sceneOrder = 4,
                sceneType = SceneType.DIALOGUE,
                description = "며느리를 만나는 장면",
                goal = "며느리의 마음 이해하기",
                sceneTitle = "걱정하는 며느리",
                imageUrl = "/files/scene-1.png",
                characterDisplayName = "방귀쟁이 며느리",
            ),
            StoryReportScene(
                sceneId = sceneTwoId,
                sceneOrder = 8,
                sceneType = SceneType.DIALOGUE,
                description = "며느리를 돕는 장면",
                goal = null,
                sceneTitle = "달라진 며느리",
                imageUrl = "/files/scene-2.png",
                characterDisplayName = "방귀쟁이 며느리",
            ),
        ),
    )

    fun childMessage(messageId: String, sceneId: String, turnOrder: Long, text: String): Message = Message(
        messageId = MessageId(messageId),
        sessionId = sessionId,
        sceneId = SceneId(sceneId),
        speakerType = SpeakerType.CHILD,
        turnOrder = turnOrder,
        text = text,
        sttRawText = text,
        audioUrl = null,
        createdAt = LocalDateTime.now().minusMinutes(10),
    )

    fun characterMessage(messageId: String, sceneId: String, turnOrder: Long): Message = Message(
        messageId = MessageId(messageId),
        sessionId = sessionId,
        sceneId = SceneId(sceneId),
        speakerType = SpeakerType.CHARACTER,
        turnOrder = turnOrder,
        text = "캐릭터 응답 $turnOrder",
        sttRawText = null,
        audioUrl = null,
        createdAt = LocalDateTime.now().minusMinutes(10),
    )

    fun analysis(messageId: String, vararg types: ThinkingElement): UtteranceAnalysis = UtteranceAnalysis(
        analysisId = AnalysisId("analysis-$messageId"),
        messageId = MessageId(messageId),
        childIntent = ChildIntent.OPINION,
        mainPoint = "핵심",
        detectedElements = types.map { DetectedElement(type = it, evidence = "근거-${it.name}") },
        validity = UtteranceValidity.VALID,
    )

    fun overall(): ReportOverall =
        ReportOverall(headline = "총평 문장", description = "총평 설명", chips = listOf("강점 칩", "확장 칩"))

    fun participation(): List<ParticipationItem> = listOf(
        ParticipationItem(title = "참여 1", description = "설명 1"),
        ParticipationItem(title = "참여 2", description = "설명 2"),
        ParticipationItem(title = "참여 3", description = "설명 3"),
    )

    fun speechAnalyses(): List<ReportSpeechAnalysis> = listOf("어휘", "표현", "논리").map { area ->
        ReportSpeechAnalysis(
            area = area,
            summary = "$area 요약",
            keywords = listOf("$area-키워드"),
            feature = "$area 특징",
            evidenceUtterance = null,
            strength = "$area 잘한 점",
            improvement = "$area 보완점",
        )
    }

    fun homeGuide(): HomeGuide = HomeGuide(
        direction = GuideDirection(headline = "방향 문장", description = "방향 설명"),
        storyQuestions = listOf(GuideQuestion(label = "이야기", question = "이야기 질문?", helper = "도움말")),
        dailyQuestions = listOf(GuideQuestion(label = "일상", question = "일상 질문?", helper = "도움말")),
        guardianTip = "보호자 팁",
    )

    fun representativeSelection(messageId: String?): RepresentativeSelection = RepresentativeSelection(
        messageId = messageId,
        situation = "이야기 상황",
        reason = "선정 이유",
        strengths = "발견한 강점",
        practiceTip = "연습 팁",
        commentary = "한 줄 해설",
        chips = listOf("대표 칩"),
    )

    fun analysisResult(
        representativeMessageId: String?,
        highlights: List<SceneHighlight> = emptyList(),
    ): ReportAnalysisResult = ReportAnalysisResult(
        overall = overall(),
        participation = participation(),
        speechAnalyses = speechAnalyses(),
        sceneHighlights = highlights,
        representative = representativeSelection(representativeMessageId),
        homeGuide = homeGuide(),
    )

    fun highlight(sceneId: String, messageId: String): SceneHighlight = SceneHighlight(
        sceneId = sceneId,
        messageId = messageId,
        featureSentence = "$sceneId 특징 문장",
        featureChips = listOf("$sceneId-칩"),
    )

    fun stubOwnedCompletedSession() {
        every { loadSpeakingSessionPort.findById(sessionId) } returns session()
        every { childAccessPort.findGuardianId(childId) } returns guardianId
    }

    fun stubAssemblySources(messages: List<Message>) {
        every { storyAccessPort.findReportSnapshot(storyId) } returns snapshot()
        every { childAccessPort.findChildName(childId) } returns childName
        every { loadMessagePort.findAllBySession(sessionId) } returns messages
        every { loadPostActivityResultPort.findBySession(sessionId) } returns null
    }

    fun stubGenerationSources(messages: List<Message>, analyses: List<UtteranceAnalysis>) {
        every { loadReportPort.findBySession(sessionId) } returns null
        stubAssemblySources(messages)
        every { loadUtteranceAnalysisPort.findByMessageIds(any()) } returns analyses
    }

    context("완료 세션 - 저장분 없음") {
        test("컨텍스트를 조립해 분석 결과의 6섹션을 저장하고 반환한다") {
            stubOwnedCompletedSession()
            val messages = listOf(
                childMessage("msg-1", sceneOneId, 1, "며느리가 힘들었을 것 같아요"),
                characterMessage("msg-2", sceneOneId, 2),
                childMessage("msg-3", sceneTwoId, 3, "제가 도와줄래요"),
            )
            val analyses = listOf(
                analysis("msg-1", ThinkingElement.EMOTION, ThinkingElement.PERSPECTIVE),
                analysis("msg-3", ThinkingElement.SOLUTION),
            )
            stubGenerationSources(messages, analyses)
            val contextSlot = slot<ReportAnalysisContext>()
            every { reportAnalysisPort.analyze(capture(contextSlot)) } returns analysisResult(
                representativeMessageId = "msg-1",
                highlights = listOf(highlight(sceneOneId, "msg-1"), highlight(sceneTwoId, "msg-3")),
            )
            val savedSlot = slot<Report>()
            every { commandReportPort.save(capture(savedSlot)) } answers { savedSlot.captured }

            val result = service.execute(command())

            result.overall shouldBe overall()
            result.participation shouldBe participation()
            result.speechAnalyses shouldBe speechAnalyses()
            result.homeGuide shouldBe homeGuide()
            result.createdAt shouldBe LocalDateTime.ofInstant(fixedInstant, ZoneOffset.UTC)
            result.summary.storyTitle shouldBe "방귀쟁이 며느리"
            result.summary.childName shouldBe childName
            result.sceneCards.map { it.sceneId to it.childUtterance.text } shouldBe
                listOf(sceneOneId to "며느리가 힘들었을 것 같아요", sceneTwoId to "제가 도와줄래요")
            result.sceneCards.map { it.sceneNumber } shouldBe listOf(1, 2)
            result.sceneCards.first().characterQuestion shouldBe null
            result.sceneCards.first().title shouldBe "걱정하는 며느리"
            result.sceneCards.last().characterQuestion shouldBe "캐릭터 응답 2"
            result.representative.text shouldBe "며느리가 힘들었을 것 같아요"
            savedSlot.captured.sessionId shouldBe sessionId
            savedSlot.captured.overall shouldBe overall()
            savedSlot.captured.participation shouldBe participation()
            savedSlot.captured.speechAnalyses shouldBe speechAnalyses()
            savedSlot.captured.sceneHighlights.map { it.sceneId to it.messageId } shouldBe
                listOf(sceneOneId to "msg-1", sceneTwoId to "msg-3")
            savedSlot.captured.representative.text shouldBe "며느리가 힘들었을 것 같아요"
            savedSlot.captured.homeGuide shouldBe homeGuide()
            verify(exactly = 1) { commandReportPort.save(any()) }

            val context = contextSlot.captured
            context.childName shouldBe childName
            context.storyTitle shouldBe "방귀쟁이 며느리"
            context.scenes.map { it.sceneId to it.goal } shouldBe
                listOf(sceneOneId to "며느리의 마음 이해하기", sceneTwoId to null)
            context.turns.map { it.messageId } shouldBe listOf("msg-1", null, "msg-3")
            context.turns.map { it.isChild } shouldBe listOf(true, false, true)
            context.analyses.map { it.messageId } shouldBe listOf("msg-1", "msg-3")
            context.analyses.first().detectedElements.map { it.type } shouldBe
                listOf(ThinkingElement.EMOTION, ThinkingElement.PERSPECTIVE)
        }

        test("발화가 없어도 예외 없이 빈 하이라이트와 anchor 없는 대표 발화로 생성한다") {
            stubOwnedCompletedSession()
            stubGenerationSources(emptyList(), emptyList())
            every { reportAnalysisPort.analyze(any()) } returns analysisResult(representativeMessageId = null)
            val savedSlot = slot<Report>()
            every { commandReportPort.save(capture(savedSlot)) } answers { savedSlot.captured }

            val result = service.execute(command())

            result.sceneCards shouldBe emptyList()
            result.representative.text shouldBe null
            result.representative.audioUrl shouldBe null
            result.overall shouldBe overall()
            verify(exactly = 1) { commandReportPort.save(any()) }
        }

        test("이야기 스냅샷이 없으면 NOT_FOUND를 던지고 저장하지 않는다") {
            stubOwnedCompletedSession()
            every { loadReportPort.findBySession(sessionId) } returns null
            every { storyAccessPort.findReportSnapshot(storyId) } returns null

            val exception = shouldThrow<NotFoundException> { service.execute(command()) }

            exception.exceptionResponseCode shouldBe NOT_FOUND
            verify(exactly = 0) { commandReportPort.save(any()) }
        }
    }

    context("보정") {
        test("분석 결과의 대표 messageId가 세션 아이 메시지가 아니면 detectedElements 최다 발화로 폴백하고 text는 실제 메시지 텍스트다") {
            stubOwnedCompletedSession()
            val messages = listOf(
                childMessage("msg-1", sceneOneId, 1, "짧은 대답"),
                childMessage("msg-2", sceneOneId, 2, "며느리 입장에서 생각하면 마음이 아파요"),
            )
            val analyses = listOf(
                analysis("msg-1", ThinkingElement.EMOTION),
                analysis("msg-2", ThinkingElement.PERSPECTIVE, ThinkingElement.EMPATHY, ThinkingElement.DECISION),
            )
            stubGenerationSources(messages, analyses)
            every { reportAnalysisPort.analyze(any()) } returns analysisResult(representativeMessageId = "msg-unknown")
            val savedSlot = slot<Report>()
            every { commandReportPort.save(capture(savedSlot)) } answers { savedSlot.captured }

            val result = service.execute(command())

            result.representative.text shouldBe "며느리 입장에서 생각하면 마음이 아파요"
            result.representative.reason shouldBe "선정 이유"
        }

        test("detectedElements 수가 같으면 turnOrder가 빠른 발화로 결정적으로 폴백한다") {
            stubOwnedCompletedSession()
            val messages = listOf(
                childMessage("msg-1", sceneOneId, 1, "첫 번째 발화"),
                childMessage("msg-2", sceneOneId, 2, "두 번째 발화"),
            )
            val analyses = listOf(
                analysis("msg-2", ThinkingElement.PERSPECTIVE),
                analysis("msg-1", ThinkingElement.EMOTION),
            )
            stubGenerationSources(messages, analyses)
            every { reportAnalysisPort.analyze(any()) } returns analysisResult(representativeMessageId = null)
            val savedSlot = slot<Report>()
            every { commandReportPort.save(capture(savedSlot)) } answers { savedSlot.captured }

            val result = service.execute(command())

            result.representative.text shouldBe "첫 번째 발화"
        }

        test("장면에 아이 발화가 여러 개면 분석 결과 지정과 무관하게 turnOrder 최대 발화를 하이라이트 messageId로 확정한다") {
            stubOwnedCompletedSession()
            val messages = listOf(
                childMessage("msg-1", sceneOneId, 1, "첫 발화"),
                characterMessage("msg-2", sceneOneId, 2),
                childMessage("msg-3", sceneOneId, 3, "마지막 발화"),
            )
            val analyses = listOf(analysis("msg-1", ThinkingElement.EMOTION))
            stubGenerationSources(messages, analyses)
            every { reportAnalysisPort.analyze(any()) } returns analysisResult(
                representativeMessageId = "msg-1",
                highlights = listOf(highlight(sceneOneId, "msg-1")),
            )
            val savedSlot = slot<Report>()
            every { commandReportPort.save(capture(savedSlot)) } answers { savedSlot.captured }

            val result = service.execute(command())

            savedSlot.captured.sceneHighlights.map { it.messageId } shouldBe listOf("msg-3")
            result.sceneCards.map { it.childUtterance.text } shouldBe listOf("마지막 발화")
            result.sceneCards.first().featureSentence shouldBe "$sceneOneId 특징 문장"
            result.sceneCards.first().featureChips shouldBe listOf("$sceneOneId-칩")
        }
    }

    context("완료 세션 - 저장분 존재") {
        test("새 구조 저장분이 있으면 재사용하고 장면 카드·메타만 라이브로 조립하며 분석·저장을 하지 않는다") {
            stubOwnedCompletedSession()
            val messages = listOf(
                childMessage("msg-1", sceneOneId, 1, "기존 대표 발화"),
            )
            stubAssemblySources(messages)
            val existing = Report(
                reportId = ReportId("report-uuid-1"),
                sessionId = sessionId,
                overall = overall(),
                participation = participation(),
                speechAnalyses = speechAnalyses(),
                sceneHighlights = listOf(highlight(sceneOneId, "msg-1")),
                representative = RepresentativeUtterance(
                    messageId = "msg-1",
                    text = "기존 대표 발화",
                    situation = "기존 상황",
                    reason = "기존 이유",
                    strengths = "기존 강점",
                    practiceTip = "기존 팁",
                    commentary = "기존 해설",
                    chips = listOf("기존 칩"),
                ),
                homeGuide = homeGuide(),
                createdAt = LocalDateTime.of(2026, 1, 1, 9, 0, 0),
            )
            every { loadReportPort.findBySession(sessionId) } returns existing

            val result = service.execute(command())

            result.overall shouldBe existing.overall
            result.representative.text shouldBe "기존 대표 발화"
            result.createdAt shouldBe existing.createdAt
            result.sceneCards.map { it.childUtterance.text } shouldBe listOf("기존 대표 발화")
            result.summary.storyTitle shouldBe "방귀쟁이 며느리"
            verify(exactly = 0) { reportAnalysisPort.analyze(any()) }
            verify(exactly = 0) { commandReportPort.save(any()) }
        }
    }

    context("게이트 실패") {
        test("status가 COMPLETED가 아니면 BUSINESS_RULE_VIOLATION을 던지고 분석·저장을 하지 않는다") {
            every { loadSpeakingSessionPort.findById(sessionId) } returns session(SessionStatus.IN_PROGRESS)
            every { childAccessPort.findGuardianId(childId) } returns guardianId

            val exception = shouldThrow<BusinessRuleViolationException> { service.execute(command()) }

            exception.exceptionResponseCode shouldBe BUSINESS_RULE_VIOLATION
            verify(exactly = 0) { loadReportPort.findBySession(any()) }
            verify(exactly = 0) { reportAnalysisPort.analyze(any()) }
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
