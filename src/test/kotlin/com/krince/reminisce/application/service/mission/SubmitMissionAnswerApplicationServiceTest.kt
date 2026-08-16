package com.krince.reminisce.application.service.mission

import com.krince.reminisce.application.port.access.child.ChildAccessPort
import com.krince.reminisce.application.port.access.story.StoryAccessPort
import com.krince.reminisce.application.port.`in`.mission.command.SubmitMissionAnswerCommand
import com.krince.reminisce.application.port.out.mission.MissionJudgePort
import com.krince.reminisce.application.port.out.mission.MissionJudgement
import com.krince.reminisce.application.port.out.missionresult.CommandMissionResultPort
import com.krince.reminisce.application.port.out.missionresult.LoadMissionResultPort
import com.krince.reminisce.application.port.out.speakingsession.LoadSpeakingSessionPort
import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.missionresult.MissionResult
import com.krince.reminisce.domain.model.missionresult.vo.MissionResultId
import com.krince.reminisce.domain.model.speakingsession.SpeakingSession
import com.krince.reminisce.domain.model.speakingsession.vo.SessionStatus
import com.krince.reminisce.domain.model.speakingsession.vo.SpeakingSessionId
import com.krince.reminisce.domain.model.story.CharacterVoice
import com.krince.reminisce.domain.model.story.Mission
import com.krince.reminisce.domain.model.story.Scene
import com.krince.reminisce.domain.model.story.VoiceAgeGroup
import com.krince.reminisce.domain.model.story.VoiceGender
import com.krince.reminisce.domain.model.story.vo.MissionType
import com.krince.reminisce.domain.model.story.vo.SceneId
import com.krince.reminisce.domain.model.story.vo.SceneType
import com.krince.reminisce.domain.model.story.vo.StoryId
import com.krince.reminisce.domain.model.story.vo.ThinkingElement
import com.krince.reminisce.domain.model.story.vo.WordCard
import com.krince.reminisce.domain.model.user.vo.UserId
import com.krince.reminisce.shared.exception.BusinessRuleViolationException
import com.krince.reminisce.shared.exception.NotFoundException
import com.krince.reminisce.shared.response.ExceptionResponseCode.BUSINESS_RULE_VIOLATION
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.time.LocalDateTime

@Tags("test", "unitTest")
@DisplayName("SubmitMissionAnswerApplicationService 단위테스트")
class SubmitMissionAnswerApplicationServiceTest : FunSpec({

    val loadSpeakingSessionPort = mockk<LoadSpeakingSessionPort>()
    val childAccessPort = mockk<ChildAccessPort>()
    val storyAccessPort = mockk<StoryAccessPort>()
    val missionJudgePort = mockk<MissionJudgePort>()
    val loadMissionResultPort = mockk<LoadMissionResultPort>()
    val commandMissionResultPort = mockk<CommandMissionResultPort>()
    val service = SubmitMissionAnswerApplicationService(
        loadSpeakingSessionPort = loadSpeakingSessionPort,
        childAccessPort = childAccessPort,
        storyAccessPort = storyAccessPort,
        missionJudgePort = missionJudgePort,
        loadMissionResultPort = loadMissionResultPort,
        commandMissionResultPort = commandMissionResultPort,
    )

    beforeEach { clearAllMocks() }

    val sessionIdStr = "session-uuid-1"
    val guardianIdStr = "guardian-uuid-1"
    val guardianId = UserId(guardianIdStr)
    val otherGuardianId = UserId("guardian-uuid-2")
    val childId = ChildId("child-uuid-1")
    val storyId = StoryId("story-uuid-1")
    val sceneIdStr = "sc_banggui_16"

    val voice = CharacterVoice(
        gender = VoiceGender.FEMALE,
        ageGroup = VoiceAgeGroup.ADULT,
        voiceProfile = "young_woman_gentle",
    )

    val wordOrderMission = Mission(
        goal = "문장 완성하기",
        examples = listOf("남들과 다른 점을 좋은 힘으로 바꿔 보세요"),
        type = MissionType.WORD_ORDER,
        wordCards = listOf(
            WordCard(text = "될 수 있어요", correctOrder = 4),
            WordCard(text = "남들과", correctOrder = 1),
            WordCard(text = "특별한 힘이", correctOrder = 3),
            WordCard(text = "달라도", correctOrder = 2),
        ),
    )
    val correctOrder = listOf("남들과", "달라도", "특별한 힘이", "될 수 있어요")

    val speakingMission = Mission(
        goal = "안전하게 배 떨어뜨리기",
        examples = listOf("무엇을 사용할지"),
        type = MissionType.SPEAKING,
    )

    fun dialogueScene(mission: Mission?): Scene = Scene(
        sceneId = SceneId(sceneIdStr),
        storyId = storyId,
        sceneOrder = 16,
        chapter = 4,
        sceneType = SceneType.DIALOGUE,
        sceneDescription = "대화 설명",
        characterName = "ch_banggui_daughter_in_law",
        characterDisplayName = "방귀쟁이 며느리",
        sceneGoal = "다름을 긍정적으로 받아들인다",
        requiredElements = listOf(ThinkingElement.EMOTION, ThinkingElement.PERSPECTIVE),
        maxTurns = 4,
        mission = mission,
        characterVoice = voice,
        characterImageUrl = "/files/char-ch_banggui_daughter_in_law.png",
    )

    fun narrationScene(): Scene = Scene(
        sceneId = SceneId(sceneIdStr),
        storyId = storyId,
        sceneOrder = 1,
        chapter = 1,
        sceneType = SceneType.NARRATION,
        sceneDescription = "내레이션 설명",
    )

    fun session(): SpeakingSession = SpeakingSession(
        sessionId = SpeakingSessionId(sessionIdStr),
        childId = childId,
        storyId = storyId,
        status = SessionStatus.IN_PROGRESS,
        startedAt = LocalDateTime.now().minusMinutes(10),
        lastActivityAt = LocalDateTime.now().minusMinutes(1),
    )

    fun command(
        submittedOrder: List<String>? = null,
        text: String? = null,
    ): SubmitMissionAnswerCommand = SubmitMissionAnswerCommand(
        sessionId = sessionIdStr,
        guardianId = guardianIdStr,
        sceneId = sceneIdStr,
        submittedOrder = submittedOrder,
        text = text,
    )

    fun savedResult(
        completed: Boolean,
        attemptCount: Int,
    ): MissionResult = MissionResult(
        id = MissionResultId("result-uuid-1"),
        sessionId = SpeakingSessionId(sessionIdStr),
        sceneId = sceneIdStr,
        completed = completed,
        attemptCount = attemptCount,
    )

    fun ownedSessionWithScene(scene: Scene) {
        every { loadSpeakingSessionPort.findById(SpeakingSessionId(sessionIdStr)) } returns session()
        every { childAccessPort.findGuardianId(childId) } returns guardianId
        every { storyAccessPort.findScene(storyId, sceneIdStr) } returns scene
    }

    context("WORD_ORDER 미션") {
        test("정답 순서를 제출하면 completed=true로 저장하고 힌트가 비어 있다") {
            ownedSessionWithScene(dialogueScene(wordOrderMission))
            every { loadMissionResultPort.findBySessionAndScene(SpeakingSessionId(sessionIdStr), sceneIdStr) } returns null
            val savedSlot = slot<MissionResult>()
            every { commandMissionResultPort.save(capture(savedSlot)) } answers { savedResult(completed = true, attemptCount = 1) }

            val result = service.execute(command(submittedOrder = correctOrder))

            result.completed shouldBe true
            result.attemptCount shouldBe 1
            result.hints shouldBe emptyList()
            savedSlot.captured.completed shouldBe true
        }

        test("오답 순서를 제출하면 미완료로 저장하고 힌트를 돌려주며 attemptCount가 증가한다") {
            val wrongOrder = listOf("달라도", "남들과", "특별한 힘이", "될 수 있어요")
            ownedSessionWithScene(dialogueScene(wordOrderMission))
            val existing = savedResult(completed = false, attemptCount = 1)
            every { loadMissionResultPort.findBySessionAndScene(SpeakingSessionId(sessionIdStr), sceneIdStr) } returns existing
            val savedSlot = slot<MissionResult>()
            every { commandMissionResultPort.save(capture(savedSlot)) } answers { savedResult(completed = false, attemptCount = 2) }

            val result = service.execute(command(submittedOrder = wrongOrder))

            result.completed shouldBe false
            result.attemptCount shouldBe 2
            result.hints shouldContainExactly wordOrderMission.examples
            savedSlot.captured.completed shouldBe false
            savedSlot.captured.attemptCount shouldBe 2
        }

        test("무제한 재시도를 위해 오답이어도 상한 없이 attemptCount를 계속 증가시킨다") {
            val wrongOrder = listOf("될 수 있어요", "특별한 힘이", "달라도", "남들과")
            ownedSessionWithScene(dialogueScene(wordOrderMission))
            val existing = savedResult(completed = false, attemptCount = 9)
            every { loadMissionResultPort.findBySessionAndScene(SpeakingSessionId(sessionIdStr), sceneIdStr) } returns existing
            val savedSlot = slot<MissionResult>()
            every { commandMissionResultPort.save(capture(savedSlot)) } answers { savedResult(completed = false, attemptCount = 10) }

            service.execute(command(submittedOrder = wrongOrder))

            savedSlot.captured.attemptCount shouldBe 10
        }
    }

    context("SPEAKING 미션") {
        test("stub 판정이 통과하면 completed=true로 저장한다") {
            ownedSessionWithScene(dialogueScene(speakingMission))
            every { missionJudgePort.judge(any()) } returns MissionJudgement(passed = true, hint = "좋아요")
            every { loadMissionResultPort.findBySessionAndScene(SpeakingSessionId(sessionIdStr), sceneIdStr) } returns null
            val savedSlot = slot<MissionResult>()
            every { commandMissionResultPort.save(capture(savedSlot)) } answers { savedResult(completed = true, attemptCount = 1) }

            val result = service.execute(command(text = "며느리에게 부탁해서 배를 떨어뜨려요"))

            result.completed shouldBe true
            savedSlot.captured.completed shouldBe true
            verify(exactly = 1) { missionJudgePort.judge("며느리에게 부탁해서 배를 떨어뜨려요") }
        }
    }

    context("게이트 실패 - 저장하지 않는다") {
        test("미션이 없는 DIALOGUE 신이면 BUSINESS_RULE_VIOLATION을 던진다") {
            ownedSessionWithScene(dialogueScene(mission = null))

            val exception = shouldThrow<BusinessRuleViolationException> { service.execute(command(submittedOrder = correctOrder)) }

            exception.exceptionResponseCode shouldBe BUSINESS_RULE_VIOLATION
            verify(exactly = 0) { commandMissionResultPort.save(any()) }
        }

        test("비-DIALOGUE 신이면 BUSINESS_RULE_VIOLATION을 던진다") {
            ownedSessionWithScene(narrationScene())

            val exception = shouldThrow<BusinessRuleViolationException> { service.execute(command(submittedOrder = correctOrder)) }

            exception.exceptionResponseCode shouldBe BUSINESS_RULE_VIOLATION
            verify(exactly = 0) { commandMissionResultPort.save(any()) }
        }

        test("존재하지 않는 신이면 BUSINESS_RULE_VIOLATION을 던진다") {
            every { loadSpeakingSessionPort.findById(SpeakingSessionId(sessionIdStr)) } returns session()
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every { storyAccessPort.findScene(storyId, sceneIdStr) } returns null

            val exception = shouldThrow<BusinessRuleViolationException> { service.execute(command(submittedOrder = correctOrder)) }

            exception.exceptionResponseCode shouldBe BUSINESS_RULE_VIOLATION
            verify(exactly = 0) { commandMissionResultPort.save(any()) }
        }

        test("세션이 없으면 NOT_FOUND를 던진다") {
            every { loadSpeakingSessionPort.findById(SpeakingSessionId(sessionIdStr)) } returns null

            val exception = shouldThrow<NotFoundException> { service.execute(command(submittedOrder = correctOrder)) }

            exception.exceptionResponseCode shouldBe NOT_FOUND
            verify(exactly = 0) { commandMissionResultPort.save(any()) }
        }

        test("타 보호자의 아이 세션이면 NOT_FOUND로 은닉하고 저장하지 않는다") {
            every { loadSpeakingSessionPort.findById(SpeakingSessionId(sessionIdStr)) } returns session()
            every { childAccessPort.findGuardianId(childId) } returns otherGuardianId

            val exception = shouldThrow<NotFoundException> { service.execute(command(submittedOrder = correctOrder)) }

            exception.exceptionResponseCode shouldBe NOT_FOUND
            verify(exactly = 0) { storyAccessPort.findScene(any(), any()) }
            verify(exactly = 0) { commandMissionResultPort.save(any()) }
        }
    }
})
