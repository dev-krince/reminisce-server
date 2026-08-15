package com.krince.reminisce.application.service.message

import com.krince.reminisce.application.port.access.child.ChildAccessPort
import com.krince.reminisce.application.port.access.story.StoryAccessPort
import com.krince.reminisce.application.port.`in`.message.command.SubmitUtteranceCommand
import com.krince.reminisce.application.port.out.analysis.SpeechAnalysisPort
import com.krince.reminisce.application.port.out.message.CommandMessagePort
import com.krince.reminisce.application.port.out.message.LoadMessagePort
import com.krince.reminisce.application.port.out.reply.CharacterReplyContext
import com.krince.reminisce.application.port.out.reply.CharacterReplyPort
import com.krince.reminisce.application.port.out.speakingsession.CommandSpeakingSessionPort
import com.krince.reminisce.application.port.out.speakingsession.LoadSpeakingSessionPort
import com.krince.reminisce.application.port.out.tts.TtsPort
import com.krince.reminisce.application.port.out.utteranceanalysis.CommandUtteranceAnalysisPort
import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.message.Message
import com.krince.reminisce.domain.model.message.vo.SpeakerType
import com.krince.reminisce.domain.model.speakingsession.SpeakingSession
import com.krince.reminisce.domain.model.speakingsession.vo.ResponseMode
import com.krince.reminisce.domain.model.speakingsession.vo.SceneEndReason
import com.krince.reminisce.domain.model.speakingsession.vo.SessionStatus
import com.krince.reminisce.domain.model.speakingsession.vo.SpeakingSessionId
import com.krince.reminisce.domain.model.story.CharacterVoice
import com.krince.reminisce.domain.model.story.Scene
import com.krince.reminisce.domain.model.story.VoiceAgeGroup
import com.krince.reminisce.domain.model.story.VoiceGender
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
    val ttsPort = mockk<TtsPort>()
    val commandMessagePort = mockk<CommandMessagePort>()
    val loadMessagePort = mockk<LoadMessagePort>()
    val speechAnalysisPort = mockk<SpeechAnalysisPort>()
    val commandUtteranceAnalysisPort = mockk<CommandUtteranceAnalysisPort>()
    val commandSpeakingSessionPort = mockk<CommandSpeakingSessionPort>()
    val characterReplyPort = mockk<CharacterReplyPort>()
    val fixedInstant = Instant.parse("2026-06-01T00:00:00Z")
    val fixedClock = Clock.fixed(fixedInstant, ZoneOffset.UTC)
    val service = SubmitUtteranceApplicationService(
        loadSpeakingSessionPort = loadSpeakingSessionPort,
        childAccessPort = childAccessPort,
        storyAccessPort = storyAccessPort,
        ttsPort = ttsPort,
        commandMessagePort = commandMessagePort,
        loadMessagePort = loadMessagePort,
        speechAnalysisPort = speechAnalysisPort,
        commandUtteranceAnalysisPort = commandUtteranceAnalysisPort,
        commandSpeakingSessionPort = commandSpeakingSessionPort,
        characterReplyPort = characterReplyPort,
        clock = fixedClock,
    )

    beforeEach {
        clearAllMocks()
        every { ttsPort.synthesize(any()) } returns "stub://tts/0"
        every { childAccessPort.findChildName(any()) } returns "지우"
        every { loadMessagePort.findRecentMessagesBySession(any(), any()) } returns emptyList()
    }

    val sessionIdStr = "session-uuid-1"
    val guardianIdStr = "guardian-uuid-1"
    val guardianId = UserId(guardianIdStr)
    val otherGuardianId = UserId("guardian-uuid-2")
    val childId = ChildId("child-uuid-1")
    val storyId = StoryId("story-uuid-1")
    val dialogueSceneIdStr = "scene-uuid-1"
    val narrationSceneIdStr = "scene-uuid-2"
    val characterLineSceneIdStr = "scene-uuid-3"
    val validText = "며느리가 참 힘들었겠어요"

    fun command(text: String = validText, sttRawText: String? = null): SubmitUtteranceCommand =
        SubmitUtteranceCommand(
            sessionId = sessionIdStr,
            guardianId = guardianIdStr,
            text = text,
            sttRawText = sttRawText,
        )

    fun session(
        currentSceneId: String?,
        status: SessionStatus = SessionStatus.IN_PROGRESS,
        sceneEndReason: SceneEndReason? = null,
    ): SpeakingSession = SpeakingSession(
        sessionId = SpeakingSessionId(sessionIdStr),
        childId = childId,
        storyId = storyId,
        status = status,
        currentSceneId = currentSceneId,
        startedAt = LocalDateTime.now(fixedClock).minusMinutes(5),
        lastActivityAt = LocalDateTime.now(fixedClock).minusMinutes(1),
        sceneEndReason = sceneEndReason,
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

    fun characterLineScene(): Scene = Scene(
        sceneId = SceneId(characterLineSceneIdStr),
        storyId = storyId,
        sceneOrder = 2,
        sceneType = SceneType.CHARACTER_LINE,
        sceneDescription = "캐릭터 대사 설명",
        characterName = "ch_x",
        characterDisplayName = "표시명",
        characterOpening = "한 줄 대사",
        characterVoice = CharacterVoice(
            gender = VoiceGender.FEMALE,
            ageGroup = VoiceAgeGroup.ADULT,
            voiceProfile = "young_woman_gentle",
        ),
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
            verify(exactly = 0) { characterReplyPort.generate(any()) }
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
            verify(exactly = 0) { characterReplyPort.generate(any()) }
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
            verify(exactly = 0) { characterReplyPort.generate(any()) }
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
            verify(exactly = 0) { characterReplyPort.generate(any()) }
        }

        test("현재 장면이 캐릭터 대사 장면이면 BUSINESS_RULE_VIOLATION을 던지고 저장하지 않는다") {
            every { loadSpeakingSessionPort.findById(SpeakingSessionId(sessionIdStr)) } returns session(characterLineSceneIdStr)
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every { storyAccessPort.findScene(storyId, characterLineSceneIdStr) } returns characterLineScene()

            val exception = shouldThrow<BusinessRuleViolationException> { service.execute(command()) }

            exception.exceptionResponseCode shouldBe BUSINESS_RULE_VIOLATION
            verify(exactly = 0) { commandMessagePort.save(any()) }
            verify(exactly = 0) { speechAnalysisPort.analyze(any()) }
            verify(exactly = 0) { commandUtteranceAnalysisPort.save(any()) }
            verify(exactly = 0) { commandSpeakingSessionPort.save(any()) }
            verify(exactly = 0) { characterReplyPort.generate(any()) }
        }

        test("status가 POST_ACTIVITY인 세션이면 BUSINESS_RULE_VIOLATION을 던지고 부작용이 없다") {
            every {
                loadSpeakingSessionPort.findById(SpeakingSessionId(sessionIdStr))
            } returns session(dialogueSceneIdStr, status = SessionStatus.POST_ACTIVITY)
            every { childAccessPort.findGuardianId(childId) } returns guardianId

            val exception = shouldThrow<BusinessRuleViolationException> { service.execute(command()) }

            exception.exceptionResponseCode shouldBe BUSINESS_RULE_VIOLATION
            verify(exactly = 0) { speechAnalysisPort.analyze(any()) }
            verify(exactly = 0) { commandMessagePort.save(any()) }
            verify(exactly = 0) { commandUtteranceAnalysisPort.save(any()) }
            verify(exactly = 0) { commandSpeakingSessionPort.save(any()) }
        }

        test("status가 COMPLETED인 세션이면 BUSINESS_RULE_VIOLATION을 던지고 부작용이 없다") {
            every {
                loadSpeakingSessionPort.findById(SpeakingSessionId(sessionIdStr))
            } returns session(dialogueSceneIdStr, status = SessionStatus.COMPLETED)
            every { childAccessPort.findGuardianId(childId) } returns guardianId

            val exception = shouldThrow<BusinessRuleViolationException> { service.execute(command()) }

            exception.exceptionResponseCode shouldBe BUSINESS_RULE_VIOLATION
            verify(exactly = 0) { speechAnalysisPort.analyze(any()) }
            verify(exactly = 0) { commandSpeakingSessionPort.save(any()) }
        }

        test("status가 STOPPED인 세션이면 BUSINESS_RULE_VIOLATION을 던지고 부작용이 없다") {
            every {
                loadSpeakingSessionPort.findById(SpeakingSessionId(sessionIdStr))
            } returns session(dialogueSceneIdStr, status = SessionStatus.STOPPED)
            every { childAccessPort.findGuardianId(childId) } returns guardianId

            val exception = shouldThrow<BusinessRuleViolationException> { service.execute(command()) }

            exception.exceptionResponseCode shouldBe BUSINESS_RULE_VIOLATION
            verify(exactly = 0) { speechAnalysisPort.analyze(any()) }
            verify(exactly = 0) { commandSpeakingSessionPort.save(any()) }
        }

        test("sceneEndReason이 세팅된 IN_PROGRESS 세션이면 BUSINESS_RULE_VIOLATION을 던지고 storyAccessPort를 호출하지 않는다") {
            every {
                loadSpeakingSessionPort.findById(SpeakingSessionId(sessionIdStr))
            } returns session(dialogueSceneIdStr, sceneEndReason = SceneEndReason.MAX_TURNS)
            every { childAccessPort.findGuardianId(childId) } returns guardianId

            val exception = shouldThrow<BusinessRuleViolationException> { service.execute(command()) }

            exception.exceptionResponseCode shouldBe BUSINESS_RULE_VIOLATION
            verify(exactly = 0) { storyAccessPort.findScene(any(), any()) }
            verify(exactly = 0) { speechAnalysisPort.analyze(any()) }
            verify(exactly = 0) { commandMessagePort.save(any()) }
            verify(exactly = 0) { commandSpeakingSessionPort.save(any()) }
        }
    }

    context("성공") {
        test("대화 장면 진행 중이면 turnOrder=count+1로 아이 발화를 저장하고 결과를 반환한다") {
            val existingCount = 2L
            val text = "며느리가 참 힘들었겠어요"
            val rawText = "며느리가 참 힘드러껬어요"
            val stubReply = "표시명: 스텁 대사"
            every { loadSpeakingSessionPort.findById(SpeakingSessionId(sessionIdStr)) } returns session(dialogueSceneIdStr)
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every { storyAccessPort.findScene(storyId, dialogueSceneIdStr) } returns dialogueScene()
            every { loadMessagePort.countBySession(SpeakingSessionId(sessionIdStr)) } returns existingCount
            val savedSlot = mutableListOf<Message>()
            every { commandMessagePort.save(capture(savedSlot)) } answers { savedSlot.last() }
            every { speechAnalysisPort.analyze(text) } returns
                rawAnalysis(listOf(DetectedElement(ThinkingElement.EMOTION, "힘들")))
            val savedAnalysisSlot = slot<UtteranceAnalysis>()
            every { commandUtteranceAnalysisPort.save(capture(savedAnalysisSlot)) } answers { savedAnalysisSlot.captured }
            val savedSessionSlot = slot<SpeakingSession>()
            every { commandSpeakingSessionPort.save(capture(savedSessionSlot)) } answers { savedSessionSlot.captured }
            every { characterReplyPort.generate(any()) } returns stubReply

            val result = service.execute(command(text = text, sttRawText = rawText))

            val childMessage = savedSlot.first()
            childMessage.speakerType shouldBe SpeakerType.CHILD
            childMessage.turnOrder shouldBe existingCount + 1
            childMessage.text shouldBe text
            childMessage.sttRawText shouldBe rawText
            childMessage.sceneId.value shouldBe dialogueSceneIdStr
            childMessage.createdAt shouldBe LocalDateTime.now(fixedClock)
            result.speakerType shouldBe SpeakerType.CHILD.name
            result.turnOrder shouldBe existingCount + 1
            result.text shouldBe text
            verify(exactly = 1) { speechAnalysisPort.analyze(text) }
            savedAnalysisSlot.captured.messageId shouldBe childMessage.messageId
            savedAnalysisSlot.captured.detectedElements.map { it.type } shouldBe listOf(ThinkingElement.EMOTION)
            savedSessionSlot.captured.accumulatedElements shouldBe listOf(ThinkingElement.EMOTION)
            savedSessionSlot.captured.lastActivityAt shouldBe LocalDateTime.now(fixedClock)
            result.detectedElements.map { it.type } shouldBe listOf(ThinkingElement.EMOTION.name)
            result.accumulatedElements shouldBe listOf(ThinkingElement.EMOTION.name)
            result.missingElements shouldBe listOf(ThinkingElement.PERSPECTIVE.name)
            result.mode shouldBe ResponseMode.NORMAL.name
            result.sceneEndReason shouldBe null
            result.sceneGoalMet shouldBe false
            result.guidanceTarget shouldBe null
            savedSessionSlot.captured.currentChildTurnCount shouldBe 1
            savedSessionSlot.captured.turnsWithoutNewElement shouldBe 0
            savedSessionSlot.captured.lastResponseMode shouldBe ResponseMode.NORMAL
            val characterMessage = savedSlot.last()
            characterMessage.speakerType shouldBe SpeakerType.CHARACTER
            characterMessage.turnOrder shouldBe childMessage.turnOrder + 1
            characterMessage.text shouldBe stubReply
            characterMessage.sttRawText shouldBe null
            result.characterReply.speakerType shouldBe SpeakerType.CHARACTER.name
            result.characterReply.turnOrder shouldBe childMessage.turnOrder + 1
            result.characterReply.text shouldBe stubReply
            result.characterReply.audio shouldBe "stub://tts/0"
            verify(exactly = 2) { commandMessagePort.save(any()) }
            verify(exactly = 1) { characterReplyPort.generate(any()) }
            verify(exactly = 1) { commandUtteranceAnalysisPort.save(any()) }
            verify(exactly = 1) { commandSpeakingSessionPort.save(any()) }
        }

        test("sttRawText 미제공이면 message.sttRawText는 null로 저장된다") {
            val text = "며느리가 참 힘들었겠어요"
            every { loadSpeakingSessionPort.findById(SpeakingSessionId(sessionIdStr)) } returns session(dialogueSceneIdStr)
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every { storyAccessPort.findScene(storyId, dialogueSceneIdStr) } returns dialogueScene()
            every { loadMessagePort.countBySession(SpeakingSessionId(sessionIdStr)) } returns 0L
            val savedSlot = mutableListOf<Message>()
            every { commandMessagePort.save(capture(savedSlot)) } answers { savedSlot.last() }
            every { speechAnalysisPort.analyze(text) } returns rawAnalysis(emptyList())
            every { commandUtteranceAnalysisPort.save(any()) } answers { firstArg() }
            every { commandSpeakingSessionPort.save(any()) } answers { firstArg() }
            every { characterReplyPort.generate(any()) } returns "표시명: 스텁 대사"

            service.execute(command(text = text, sttRawText = null))

            val childMessage = savedSlot.first()
            childMessage.text shouldBe text
            childMessage.sttRawText shouldBe null
        }

        test("근거 없는 요소는 폐기되어 누적되지 않는다") {
            val text = "며느리가 참 힘들었겠어요"
            every { loadSpeakingSessionPort.findById(SpeakingSessionId(sessionIdStr)) } returns session(dialogueSceneIdStr)
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every { storyAccessPort.findScene(storyId, dialogueSceneIdStr) } returns dialogueScene()
            every { loadMessagePort.countBySession(SpeakingSessionId(sessionIdStr)) } returns 0L
            every { commandMessagePort.save(any()) } answers { firstArg() }
            every { speechAnalysisPort.analyze(text) } returns
                rawAnalysis(listOf(DetectedElement(ThinkingElement.PERSPECTIVE, "원문에 없는 근거")))
            val savedSessionSlot = slot<SpeakingSession>()
            every { commandUtteranceAnalysisPort.save(any()) } answers { firstArg() }
            every { commandSpeakingSessionPort.save(capture(savedSessionSlot)) } answers { savedSessionSlot.captured }
            every { characterReplyPort.generate(any()) } returns "표시명: 스텁 대사"

            val result = service.execute(command(text = text))

            savedSessionSlot.captured.accumulatedElements shouldBe emptyList()
            result.detectedElements shouldBe emptyList()
            result.missingElements shouldBe listOf(ThinkingElement.PERSPECTIVE.name)
        }

        test("첫 발화면 turnOrder=1이 된다") {
            val text = "안녕하세요"
            every { loadSpeakingSessionPort.findById(SpeakingSessionId(sessionIdStr)) } returns session(dialogueSceneIdStr)
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every { storyAccessPort.findScene(storyId, dialogueSceneIdStr) } returns dialogueScene()
            every { loadMessagePort.countBySession(SpeakingSessionId(sessionIdStr)) } returns 0L
            val savedSlot = mutableListOf<Message>()
            every { commandMessagePort.save(capture(savedSlot)) } answers { savedSlot.last() }
            every { speechAnalysisPort.analyze(text) } returns rawAnalysis(emptyList())
            every { commandUtteranceAnalysisPort.save(any()) } answers { firstArg() }
            every { commandSpeakingSessionPort.save(any()) } answers { firstArg() }
            every { characterReplyPort.generate(any()) } returns "표시명: 스텁 대사"

            val result = service.execute(command(text = text))

            savedSlot.first().turnOrder shouldBe 1L
            result.turnOrder shouldBe 1L
        }
    }

    context("캐릭터 반응") {
        test("mode=GUIDED면 포트로 대사를 생성하고 유도 대상을 전달해 character 메시지로 저장한다") {
            val existingCount = 1L
            val transcript = "며느리가 참 힘들었겠어요"
            val stubReply = "표시명: 유도 대사"
            val lowInfoSession = session(dialogueSceneIdStr).copy(
                currentChildTurnCount = 1,
                turnsWithoutNewElement = 1,
            )
            every { loadSpeakingSessionPort.findById(SpeakingSessionId(sessionIdStr)) } returns lowInfoSession
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every { storyAccessPort.findScene(storyId, dialogueSceneIdStr) } returns dialogueScene()
            every { loadMessagePort.countBySession(SpeakingSessionId(sessionIdStr)) } returns existingCount
            val savedSlot = mutableListOf<Message>()
            every { commandMessagePort.save(capture(savedSlot)) } answers { savedSlot.last() }
            every { speechAnalysisPort.analyze(transcript) } returns rawAnalysis(emptyList())
            every { commandUtteranceAnalysisPort.save(any()) } answers { firstArg() }
            every { commandSpeakingSessionPort.save(any()) } answers { firstArg() }
            val contextSlot = slot<CharacterReplyContext>()
            every { characterReplyPort.generate(capture(contextSlot)) } returns stubReply

            val result = service.execute(command(text = transcript))

            result.mode shouldBe ResponseMode.GUIDED.name
            contextSlot.captured.mode shouldBe ResponseMode.GUIDED
            contextSlot.captured.characterDisplayName shouldBe "표시명"
            contextSlot.captured.guidanceTarget shouldBe ThinkingElement.PERSPECTIVE
            savedSlot.last().speakerType shouldBe SpeakerType.CHARACTER
            savedSlot.last().text shouldBe stubReply
            result.characterReply.text shouldBe stubReply
            verify(exactly = 1) { characterReplyPort.generate(any()) }
        }

        test("mode=CLOSING이면 포트를 호출하지 않고 characterClosing을 character 메시지 text로 저장한다") {
            val existingCount = 3L
            val transcript = "며느리가 참 힘들었겠어요"
            val closingSession = session(dialogueSceneIdStr).copy(currentChildTurnCount = 3)
            every { loadSpeakingSessionPort.findById(SpeakingSessionId(sessionIdStr)) } returns closingSession
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every { storyAccessPort.findScene(storyId, dialogueSceneIdStr) } returns dialogueScene()
            every { loadMessagePort.countBySession(SpeakingSessionId(sessionIdStr)) } returns existingCount
            val savedSlot = mutableListOf<Message>()
            every { commandMessagePort.save(capture(savedSlot)) } answers { savedSlot.last() }
            every { speechAnalysisPort.analyze(transcript) } returns
                rawAnalysis(listOf(DetectedElement(ThinkingElement.EMOTION, "힘들")))
            every { commandUtteranceAnalysisPort.save(any()) } answers { firstArg() }
            every { commandSpeakingSessionPort.save(any()) } answers { firstArg() }

            val result = service.execute(command(text = transcript))

            result.mode shouldBe ResponseMode.CLOSING.name
            val childMessage = savedSlot.first()
            val characterMessage = savedSlot.last()
            characterMessage.speakerType shouldBe SpeakerType.CHARACTER
            characterMessage.turnOrder shouldBe childMessage.turnOrder + 1
            characterMessage.text shouldBe dialogueScene().characterClosing
            result.characterReply.text shouldBe dialogueScene().characterClosing
            verify(exactly = 0) { characterReplyPort.generate(any()) }
        }
    }
})
