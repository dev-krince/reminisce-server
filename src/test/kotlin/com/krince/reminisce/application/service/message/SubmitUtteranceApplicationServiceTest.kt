package com.krince.reminisce.application.service.message

import com.krince.reminisce.application.port.access.child.ChildAccessPort
import com.krince.reminisce.application.port.access.story.StoryAccessPort
import com.krince.reminisce.application.port.`in`.message.command.SubmitUtteranceCommand
import com.krince.reminisce.application.port.out.analysis.SpeechAnalysisPort
import com.krince.reminisce.application.port.out.message.CommandMessagePort
import com.krince.reminisce.application.port.out.message.LoadMessagePort
import com.krince.reminisce.application.port.out.speakingsession.CommandSpeakingSessionPort
import com.krince.reminisce.application.port.out.speakingsession.LoadSpeakingSessionPort
import com.krince.reminisce.application.port.out.stt.SttPort
import com.krince.reminisce.application.port.out.utteranceanalysis.CommandUtteranceAnalysisPort
import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.message.Message
import com.krince.reminisce.domain.model.message.vo.SpeakerType
import com.krince.reminisce.domain.model.speakingsession.SpeakingSession
import com.krince.reminisce.domain.model.speakingsession.vo.SessionStatus
import com.krince.reminisce.domain.model.speakingsession.vo.SpeakingSessionId
import com.krince.reminisce.domain.model.story.Scene
import com.krince.reminisce.domain.model.story.vo.SceneId
import com.krince.reminisce.domain.model.story.vo.SceneType
import com.krince.reminisce.domain.model.story.vo.StoryId
import com.krince.reminisce.domain.model.story.vo.ThinkingElement
import com.krince.reminisce.domain.model.user.vo.UserId
import com.krince.reminisce.domain.model.utteranceanalysis.DetectedElement
import com.krince.reminisce.domain.model.utteranceanalysis.RawUtteranceAnalysis
import com.krince.reminisce.domain.model.utteranceanalysis.UtteranceAnalysis
import com.krince.reminisce.domain.model.utteranceanalysis.vo.ChildIntent
import com.krince.reminisce.domain.model.utteranceanalysis.vo.UtteranceValidity
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
import io.mockk.slot
import io.mockk.verify
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

@Tags("test", "unitTest")
@DisplayName("SubmitUtteranceApplicationService 단위테스트")
class SubmitUtteranceApplicationServiceTest : FunSpec({

    val loadSpeakingSessionPort = mockk<LoadSpeakingSessionPort>()
    val childAccessPort = mockk<ChildAccessPort>()
    val storyAccessPort = mockk<StoryAccessPort>()
    val sttPort = mockk<SttPort>()
    val commandMessagePort = mockk<CommandMessagePort>()
    val loadMessagePort = mockk<LoadMessagePort>()
    val speechAnalysisPort = mockk<SpeechAnalysisPort>()
    val commandUtteranceAnalysisPort = mockk<CommandUtteranceAnalysisPort>()
    val commandSpeakingSessionPort = mockk<CommandSpeakingSessionPort>()
    val fixedInstant = Instant.parse("2026-06-01T00:00:00Z")
    val fixedClock = Clock.fixed(fixedInstant, ZoneOffset.UTC)
    val service = SubmitUtteranceApplicationService(
        loadSpeakingSessionPort = loadSpeakingSessionPort,
        childAccessPort = childAccessPort,
        storyAccessPort = storyAccessPort,
        sttPort = sttPort,
        commandMessagePort = commandMessagePort,
        loadMessagePort = loadMessagePort,
        speechAnalysisPort = speechAnalysisPort,
        commandUtteranceAnalysisPort = commandUtteranceAnalysisPort,
        commandSpeakingSessionPort = commandSpeakingSessionPort,
        clock = fixedClock,
    )

    beforeEach { clearAllMocks() }

    val sessionIdStr = "session-uuid-1"
    val guardianIdStr = "guardian-uuid-1"
    val guardianId = UserId(guardianIdStr)
    val otherGuardianId = UserId("guardian-uuid-2")
    val childId = ChildId("child-uuid-1")
    val storyId = StoryId("story-uuid-1")
    val dialogueSceneIdStr = "scene-uuid-1"
    val narrationSceneIdStr = "scene-uuid-2"
    val validAudio = "며느리가 참 힘들었겠어요"

    fun command(audio: String = validAudio): SubmitUtteranceCommand =
        SubmitUtteranceCommand(sessionId = sessionIdStr, guardianId = guardianIdStr, audio = audio)

    fun session(currentSceneId: String?): SpeakingSession = SpeakingSession(
        sessionId = SpeakingSessionId(sessionIdStr),
        childId = childId,
        storyId = storyId,
        status = SessionStatus.IN_PROGRESS,
        currentSceneId = currentSceneId,
        startedAt = LocalDateTime.now(fixedClock).minusMinutes(5),
        lastActivityAt = LocalDateTime.now(fixedClock).minusMinutes(1),
    )

    fun dialogueScene(): Scene = Scene(
        sceneId = SceneId(dialogueSceneIdStr),
        storyId = storyId,
        sceneOrder = 3,
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

    fun narrationScene(): Scene = Scene(
        sceneId = SceneId(narrationSceneIdStr),
        storyId = storyId,
        sceneOrder = 1,
        sceneType = SceneType.NARRATION,
        sceneDescription = "전개 설명",
    )

    fun rawAnalysis(detectedElements: List<DetectedElement>): RawUtteranceAnalysis = RawUtteranceAnalysis(
        childIntent = ChildIntent.PERSPECTIVE,
        mainPoint = "며느리가 힘들었을 것이다",
        detectedElements = detectedElements,
        validity = UtteranceValidity.VALID,
    )

    context("게이트 실패") {
        test("세션이 없으면 NOT_FOUND를 던지고 저장하지 않는다") {
            every { loadSpeakingSessionPort.findById(SpeakingSessionId(sessionIdStr)) } returns null

            val exception = shouldThrow<NotFoundException> { service.execute(command()) }

            exception.exceptionResponseCode shouldBe NOT_FOUND
            verify(exactly = 0) { commandMessagePort.save(any()) }
            verify(exactly = 0) { speechAnalysisPort.analyze(any()) }
            verify(exactly = 0) { commandUtteranceAnalysisPort.save(any()) }
            verify(exactly = 0) { commandSpeakingSessionPort.save(any()) }
        }

        test("타 보호자의 아이 세션이면 NOT_FOUND로 은닉하고 저장하지 않는다") {
            every { loadSpeakingSessionPort.findById(SpeakingSessionId(sessionIdStr)) } returns session(dialogueSceneIdStr)
            every { childAccessPort.findGuardianId(childId) } returns otherGuardianId

            val exception = shouldThrow<NotFoundException> { service.execute(command()) }

            exception.exceptionResponseCode shouldBe NOT_FOUND
            verify(exactly = 0) { commandMessagePort.save(any()) }
            verify(exactly = 0) { speechAnalysisPort.analyze(any()) }
            verify(exactly = 0) { commandUtteranceAnalysisPort.save(any()) }
            verify(exactly = 0) { commandSpeakingSessionPort.save(any()) }
        }

        test("도입 상태(currentSceneId null)면 BUSINESS_RULE_VIOLATION을 던지고 저장하지 않는다") {
            every { loadSpeakingSessionPort.findById(SpeakingSessionId(sessionIdStr)) } returns session(null)
            every { childAccessPort.findGuardianId(childId) } returns guardianId

            val exception = shouldThrow<BusinessRuleViolationException> { service.execute(command()) }

            exception.exceptionResponseCode shouldBe BUSINESS_RULE_VIOLATION
            verify(exactly = 0) { commandMessagePort.save(any()) }
            verify(exactly = 0) { speechAnalysisPort.analyze(any()) }
            verify(exactly = 0) { commandUtteranceAnalysisPort.save(any()) }
            verify(exactly = 0) { commandSpeakingSessionPort.save(any()) }
        }

        test("현재 장면이 대화 장면이 아니면 BUSINESS_RULE_VIOLATION을 던지고 저장하지 않는다") {
            every { loadSpeakingSessionPort.findById(SpeakingSessionId(sessionIdStr)) } returns session(narrationSceneIdStr)
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every { storyAccessPort.findScene(storyId, narrationSceneIdStr) } returns narrationScene()

            val exception = shouldThrow<BusinessRuleViolationException> { service.execute(command()) }

            exception.exceptionResponseCode shouldBe BUSINESS_RULE_VIOLATION
            verify(exactly = 0) { commandMessagePort.save(any()) }
            verify(exactly = 0) { speechAnalysisPort.analyze(any()) }
            verify(exactly = 0) { commandUtteranceAnalysisPort.save(any()) }
            verify(exactly = 0) { commandSpeakingSessionPort.save(any()) }
        }

        test("STT가 실패(null)하면 STT_TRANSCRIPTION_FAILED를 던지고 저장하지 않는다") {
            every { loadSpeakingSessionPort.findById(SpeakingSessionId(sessionIdStr)) } returns session(dialogueSceneIdStr)
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every { storyAccessPort.findScene(storyId, dialogueSceneIdStr) } returns dialogueScene()
            every { sttPort.transcribe(any()) } returns null

            val exception = shouldThrow<BusinessRuleViolationException> { service.execute(command(audio = "   ")) }

            exception.exceptionResponseCode shouldBe STT_TRANSCRIPTION_FAILED
            verify(exactly = 0) { commandMessagePort.save(any()) }
            verify(exactly = 0) { speechAnalysisPort.analyze(any()) }
            verify(exactly = 0) { commandUtteranceAnalysisPort.save(any()) }
            verify(exactly = 0) { commandSpeakingSessionPort.save(any()) }
        }
    }

    context("성공") {
        test("대화 장면 진행 중이면 turnOrder=count+1로 아이 발화를 저장하고 결과를 반환한다") {
            val existingCount = 2L
            val transcript = "며느리가 참 힘들었겠어요"
            every { loadSpeakingSessionPort.findById(SpeakingSessionId(sessionIdStr)) } returns session(dialogueSceneIdStr)
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every { storyAccessPort.findScene(storyId, dialogueSceneIdStr) } returns dialogueScene()
            every { sttPort.transcribe(validAudio) } returns transcript
            every { loadMessagePort.countBySession(SpeakingSessionId(sessionIdStr)) } returns existingCount
            val savedSlot = slot<Message>()
            every { commandMessagePort.save(capture(savedSlot)) } answers { savedSlot.captured }
            every { speechAnalysisPort.analyze(transcript) } returns
                rawAnalysis(listOf(DetectedElement(ThinkingElement.EMOTION, "힘들")))
            val savedAnalysisSlot = slot<UtteranceAnalysis>()
            every { commandUtteranceAnalysisPort.save(capture(savedAnalysisSlot)) } answers { savedAnalysisSlot.captured }
            val savedSessionSlot = slot<SpeakingSession>()
            every { commandSpeakingSessionPort.save(capture(savedSessionSlot)) } answers { savedSessionSlot.captured }

            val result = service.execute(command())

            savedSlot.captured.speakerType shouldBe SpeakerType.CHILD
            savedSlot.captured.turnOrder shouldBe existingCount + 1
            savedSlot.captured.text shouldBe transcript
            savedSlot.captured.sttRawText shouldBe transcript
            savedSlot.captured.sceneId.value shouldBe dialogueSceneIdStr
            savedSlot.captured.createdAt shouldBe LocalDateTime.now(fixedClock)
            result.speakerType shouldBe SpeakerType.CHILD.name
            result.turnOrder shouldBe existingCount + 1
            result.text shouldBe transcript
            savedAnalysisSlot.captured.messageId shouldBe savedSlot.captured.messageId
            savedAnalysisSlot.captured.detectedElements.map { it.type } shouldBe listOf(ThinkingElement.EMOTION)
            savedSessionSlot.captured.accumulatedElements shouldBe listOf(ThinkingElement.EMOTION)
            savedSessionSlot.captured.lastActivityAt shouldBe LocalDateTime.now(fixedClock)
            result.detectedElements.map { it.type } shouldBe listOf(ThinkingElement.EMOTION.name)
            result.accumulatedElements shouldBe listOf(ThinkingElement.EMOTION.name)
            result.missingElements shouldBe listOf(ThinkingElement.PERSPECTIVE.name)
            verify(exactly = 1) { commandMessagePort.save(any()) }
            verify(exactly = 1) { commandUtteranceAnalysisPort.save(any()) }
            verify(exactly = 1) { commandSpeakingSessionPort.save(any()) }
        }

        test("근거 없는 요소는 폐기되어 누적되지 않는다") {
            val transcript = "며느리가 참 힘들었겠어요"
            every { loadSpeakingSessionPort.findById(SpeakingSessionId(sessionIdStr)) } returns session(dialogueSceneIdStr)
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every { storyAccessPort.findScene(storyId, dialogueSceneIdStr) } returns dialogueScene()
            every { sttPort.transcribe(validAudio) } returns transcript
            every { loadMessagePort.countBySession(SpeakingSessionId(sessionIdStr)) } returns 0L
            val savedSlot = slot<Message>()
            every { commandMessagePort.save(capture(savedSlot)) } answers { savedSlot.captured }
            every { speechAnalysisPort.analyze(transcript) } returns
                rawAnalysis(listOf(DetectedElement(ThinkingElement.PERSPECTIVE, "원문에 없는 근거")))
            val savedSessionSlot = slot<SpeakingSession>()
            every { commandUtteranceAnalysisPort.save(any()) } answers { firstArg() }
            every { commandSpeakingSessionPort.save(capture(savedSessionSlot)) } answers { savedSessionSlot.captured }

            val result = service.execute(command())

            savedSessionSlot.captured.accumulatedElements shouldBe emptyList()
            result.detectedElements shouldBe emptyList()
            result.missingElements shouldBe listOf(ThinkingElement.PERSPECTIVE.name)
        }

        test("첫 발화면 turnOrder=1이 된다") {
            val transcript = "안녕하세요"
            every { loadSpeakingSessionPort.findById(SpeakingSessionId(sessionIdStr)) } returns session(dialogueSceneIdStr)
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every { storyAccessPort.findScene(storyId, dialogueSceneIdStr) } returns dialogueScene()
            every { sttPort.transcribe(validAudio) } returns transcript
            every { loadMessagePort.countBySession(SpeakingSessionId(sessionIdStr)) } returns 0L
            val savedSlot = slot<Message>()
            every { commandMessagePort.save(capture(savedSlot)) } answers { savedSlot.captured }
            every { speechAnalysisPort.analyze(transcript) } returns rawAnalysis(emptyList())
            every { commandUtteranceAnalysisPort.save(any()) } answers { firstArg() }
            every { commandSpeakingSessionPort.save(any()) } answers { firstArg() }

            val result = service.execute(command())

            savedSlot.captured.turnOrder shouldBe 1L
            result.turnOrder shouldBe 1L
        }
    }
})
