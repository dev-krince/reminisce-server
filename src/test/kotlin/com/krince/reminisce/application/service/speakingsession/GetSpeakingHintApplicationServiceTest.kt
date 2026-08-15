package com.krince.reminisce.application.service.speakingsession

import com.krince.reminisce.application.port.access.child.ChildAccessPort
import com.krince.reminisce.application.port.access.story.StoryAccessPort
import com.krince.reminisce.application.port.`in`.speakingsession.command.GetSpeakingHintCommand
import com.krince.reminisce.application.port.out.speakingsession.LoadSpeakingSessionPort
import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.speakingsession.SpeakingSession
import com.krince.reminisce.domain.model.speakingsession.vo.SessionStatus
import com.krince.reminisce.domain.model.speakingsession.vo.SpeakingSessionId
import com.krince.reminisce.domain.model.story.CharacterVoice
import com.krince.reminisce.domain.model.story.Mission
import com.krince.reminisce.domain.model.story.Scene
import com.krince.reminisce.domain.model.story.VoiceAgeGroup
import com.krince.reminisce.domain.model.story.VoiceGender
import com.krince.reminisce.domain.model.story.vo.SceneId
import com.krince.reminisce.domain.model.story.vo.SceneType
import com.krince.reminisce.domain.model.story.vo.StoryId
import com.krince.reminisce.domain.model.story.vo.ThinkingElement
import com.krince.reminisce.domain.model.user.vo.UserId
import com.krince.reminisce.shared.exception.NotFoundException
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
@DisplayName("GetSpeakingHintApplicationService 단위테스트")
class GetSpeakingHintApplicationServiceTest : FunSpec({

    val loadSpeakingSessionPort = mockk<LoadSpeakingSessionPort>()
    val childAccessPort = mockk<ChildAccessPort>()
    val storyAccessPort = mockk<StoryAccessPort>()
    val service = GetSpeakingHintApplicationService(loadSpeakingSessionPort, childAccessPort, storyAccessPort)

    beforeEach { clearAllMocks() }

    val sessionIdStr = "session-uuid-1"
    val guardianIdStr = "guardian-uuid-1"
    val childId = ChildId("child-uuid-1")
    val storyId = StoryId("story-uuid-1")
    val sceneIdStr = "scene-uuid-1"

    fun command(guardian: String = guardianIdStr): GetSpeakingHintCommand =
        GetSpeakingHintCommand(sessionId = sessionIdStr, guardianId = guardian)

    fun session(currentSceneId: String? = sceneIdStr): SpeakingSession = SpeakingSession(
        sessionId = SpeakingSessionId(sessionIdStr),
        childId = childId,
        storyId = storyId,
        status = SessionStatus.IN_PROGRESS,
        currentSceneId = currentSceneId,
        startedAt = LocalDateTime.parse("2026-06-01T00:00:00"),
        lastActivityAt = LocalDateTime.parse("2026-06-01T00:05:00"),
        sceneEndReason = null,
    )

    fun scene(mission: Mission?): Scene = Scene(
        sceneId = SceneId(sceneIdStr),
        storyId = storyId,
        sceneOrder = 1,
        sceneType = SceneType.DIALOGUE,
        sceneDescription = "설명",
        characterName = "ch",
        characterDisplayName = "며느리",
        sceneGoal = "목표",
        requiredElements = listOf(ThinkingElement.PERSPECTIVE),
        maxTurns = 4,
        mission = mission,
        characterVoice = CharacterVoice(
            gender = VoiceGender.FEMALE,
            ageGroup = VoiceAgeGroup.ADULT,
            voiceProfile = "young_woman_gentle",
        ),
        characterImageUrl = "/files/char-ch.png",
    )

    test("소유한 세션의 현재 장면 미션 예시를 힌트로 반환한다") {
        every { loadSpeakingSessionPort.findById(SpeakingSessionId(sessionIdStr)) } returns session()
        every { childAccessPort.findGuardianId(childId) } returns UserId(guardianIdStr)
        every { storyAccessPort.findScene(storyId, sceneIdStr) } returns
            scene(Mission("며느리 마음 헤아리기", listOf("왜 힘든지 말해볼까요?", "어떻게 도와줄 수 있을까요?")))

        val result = service.execute(command())

        result.goal shouldBe "며느리 마음 헤아리기"
        result.hints shouldBe listOf("왜 힘든지 말해볼까요?", "어떻게 도와줄 수 있을까요?")
    }

    test("미션이 없는 장면은 빈 힌트를 반환한다") {
        every { loadSpeakingSessionPort.findById(SpeakingSessionId(sessionIdStr)) } returns session()
        every { childAccessPort.findGuardianId(childId) } returns UserId(guardianIdStr)
        every { storyAccessPort.findScene(storyId, sceneIdStr) } returns scene(null)

        val result = service.execute(command())

        result.goal shouldBe null
        result.hints shouldBe emptyList()
    }

    test("현재 장면이 없으면 빈 힌트를 반환한다") {
        every { loadSpeakingSessionPort.findById(SpeakingSessionId(sessionIdStr)) } returns session(currentSceneId = null)
        every { childAccessPort.findGuardianId(childId) } returns UserId(guardianIdStr)

        val result = service.execute(command())

        result.hints shouldBe emptyList()
    }

    test("다른 보호자의 세션이면 NotFound") {
        every { loadSpeakingSessionPort.findById(SpeakingSessionId(sessionIdStr)) } returns session()
        every { childAccessPort.findGuardianId(childId) } returns UserId("other-guardian")

        shouldThrow<NotFoundException> { service.execute(command()) }
    }

    test("세션이 없으면 NotFound") {
        every { loadSpeakingSessionPort.findById(SpeakingSessionId(sessionIdStr)) } returns null

        shouldThrow<NotFoundException> { service.execute(command()) }
    }
})
