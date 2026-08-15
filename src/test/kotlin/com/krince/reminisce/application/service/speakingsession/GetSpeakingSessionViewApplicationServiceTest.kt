package com.krince.reminisce.application.service.speakingsession

import com.krince.reminisce.application.port.access.child.ChildAccessPort
import com.krince.reminisce.application.port.access.story.StoryAccessPort
import com.krince.reminisce.application.port.`in`.speakingsession.command.GetSpeakingSessionViewCommand
import com.krince.reminisce.application.port.`in`.speakingsession.result.SpeakingSessionViewType
import com.krince.reminisce.application.port.out.speakingsession.LoadSpeakingSessionPort
import com.krince.reminisce.application.port.out.tts.TtsPort
import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.speakingsession.SpeakingSession
import com.krince.reminisce.domain.model.speakingsession.vo.SessionStatus
import com.krince.reminisce.domain.model.speakingsession.vo.SpeakingSessionId
import com.krince.reminisce.domain.model.story.Scene
import com.krince.reminisce.domain.model.story.vo.SceneId
import com.krince.reminisce.domain.model.story.vo.SceneType
import com.krince.reminisce.domain.model.story.vo.StoryId
import com.krince.reminisce.domain.model.story.vo.ThinkingElement
import io.kotest.matchers.shouldNotBe
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
@DisplayName("GetSpeakingSessionViewApplicationService 단위테스트")
class GetSpeakingSessionViewApplicationServiceTest : FunSpec({

    val loadSpeakingSessionPort = mockk<LoadSpeakingSessionPort>()
    val childAccessPort = mockk<ChildAccessPort>()
    val storyAccessPort = mockk<StoryAccessPort>()
    val ttsPort = mockk<TtsPort>()
    val service = GetSpeakingSessionViewApplicationService(
        loadSpeakingSessionPort = loadSpeakingSessionPort,
        childAccessPort = childAccessPort,
        storyAccessPort = storyAccessPort,
        ttsPort = ttsPort,
    )

    beforeEach {
        clearAllMocks()
        every { ttsPort.synthesize(any(), any()) } returns "stub://tts/0"
        every { childAccessPort.findChildName(any()) } returns "지우"
    }

    val sessionIdStr = "session-uuid-1"
    val guardianIdStr = "guardian-uuid-1"
    val guardianId = UserId(guardianIdStr)
    val otherGuardianId = UserId("guardian-uuid-2")
    val childId = ChildId("child-uuid-1")
    val storyId = StoryId("story-uuid-1")
    val firstSceneIdStr = "scene-uuid-1"
    val introText = "옛날 어느 마을에 방귀쟁이 며느리가 살았습니다."
    val sceneImageUrl = "/files/test-scene.png"

    fun command(): GetSpeakingSessionViewCommand =
        GetSpeakingSessionViewCommand(sessionId = sessionIdStr, guardianId = guardianIdStr)

    fun session(currentSceneId: String?): SpeakingSession = SpeakingSession(
        sessionId = SpeakingSessionId(sessionIdStr),
        childId = childId,
        storyId = storyId,
        status = SessionStatus.IN_PROGRESS,
        currentSceneId = currentSceneId,
        startedAt = LocalDateTime.now().minusMinutes(5),
        lastActivityAt = LocalDateTime.now().minusMinutes(1),
    )

    fun dialogueScene(): Scene = Scene(
        sceneId = SceneId(firstSceneIdStr),
        storyId = storyId,
        sceneOrder = 1,
        sceneType = SceneType.DIALOGUE,
        sceneDescription = "대화 설명",
        characterName = "ch_x",
        characterDisplayName = "표시명",
        characterOpening = "여는 대사",
        characterClosing = "닫는 대사",
        sceneGoal = "목표",
        requiredElements = listOf(ThinkingElement.PERSPECTIVE),
        maxTurns = 4,
        imageUrl = sceneImageUrl,
    )

    fun narrationScene(): Scene = Scene(
        sceneId = SceneId(firstSceneIdStr),
        storyId = storyId,
        sceneOrder = 1,
        sceneType = SceneType.NARRATION,
        sceneDescription = "전개 설명",
        imageUrl = sceneImageUrl,
    )

    fun placeholderScene(): Scene = Scene(
        sceneId = SceneId(firstSceneIdStr),
        storyId = storyId,
        sceneOrder = 1,
        sceneType = SceneType.DIALOGUE,
        sceneDescription = "대화 설명",
        characterName = "ch_x",
        characterDisplayName = "표시명",
        characterOpening = "ㅇㅇ아, 안녕?",
        characterClosing = "ㅇㅇ이 덕분에 좋았어.",
        sceneGoal = "목표",
        requiredElements = listOf(ThinkingElement.PERSPECTIVE),
        maxTurns = 4,
    )

    context("게이트 실패") {
        test("세션이 없으면 NOT_FOUND를 던진다") {
            every { loadSpeakingSessionPort.findById(SpeakingSessionId(sessionIdStr)) } returns null

            val exception = shouldThrow<NotFoundException> { service.execute(command()) }

            exception.exceptionResponseCode shouldBe NOT_FOUND
        }

        test("타 보호자의 아이 세션이면 NOT_FOUND로 은닉한다") {
            every { loadSpeakingSessionPort.findById(SpeakingSessionId(sessionIdStr)) } returns session(null)
            every { childAccessPort.findGuardianId(childId) } returns otherGuardianId

            val exception = shouldThrow<NotFoundException> { service.execute(command()) }

            exception.exceptionResponseCode shouldBe NOT_FOUND
        }
    }

    context("성공") {
        test("도입 상태 세션이면 INTRO 뷰와 intro 텍스트를 반환한다") {
            every { loadSpeakingSessionPort.findById(SpeakingSessionId(sessionIdStr)) } returns session(null)
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every { storyAccessPort.findIntro(storyId) } returns introText

            val result = service.execute(command())

            result.viewType shouldBe SpeakingSessionViewType.INTRO
            result.intro shouldBe introText
            result.introAudio shouldNotBe null
            result.scene shouldBe null
        }

        test("장면 상태 세션이면 SCENE 뷰와 현재 장면을 반환한다") {
            every { loadSpeakingSessionPort.findById(SpeakingSessionId(sessionIdStr)) } returns session(firstSceneIdStr)
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every { storyAccessPort.findScene(storyId, firstSceneIdStr) } returns dialogueScene()

            val result = service.execute(command())

            result.viewType shouldBe SpeakingSessionViewType.SCENE
            result.intro shouldBe null
            result.scene?.sceneId shouldBe firstSceneIdStr
            result.scene?.sceneType shouldBe SceneType.DIALOGUE
            result.scene?.imageUrl shouldBe sceneImageUrl
        }

        test("DIALOGUE 장면이면 characterOpeningAudio·characterClosingAudio가 non-null이다") {
            every { loadSpeakingSessionPort.findById(SpeakingSessionId(sessionIdStr)) } returns session(firstSceneIdStr)
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every { storyAccessPort.findScene(storyId, firstSceneIdStr) } returns dialogueScene()
            every { ttsPort.synthesize("여는 대사") } returns "stub://tts/opening"
            every { ttsPort.synthesize("닫는 대사") } returns "stub://tts/closing"

            val result = service.execute(command())

            result.scene?.characterOpeningAudio shouldNotBe null
            result.scene?.characterClosingAudio shouldNotBe null
            result.scene?.narrationAudio shouldBe null
        }

        test("장면 대사의 ㅇㅇ 자리표시자를 아이 이름과 조사로 치환한다") {
            every { loadSpeakingSessionPort.findById(SpeakingSessionId(sessionIdStr)) } returns session(firstSceneIdStr)
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every { childAccessPort.findChildName(childId) } returns "지우"
            every { storyAccessPort.findScene(storyId, firstSceneIdStr) } returns placeholderScene()

            val result = service.execute(command())

            result.scene?.characterOpening shouldBe "지우야, 안녕?"
            result.scene?.characterClosing shouldBe "지우 덕분에 좋았어."
        }

        test("NARRATION 장면이면 characterOpeningAudio·characterClosingAudio가 null이다") {
            every { loadSpeakingSessionPort.findById(SpeakingSessionId(sessionIdStr)) } returns session(firstSceneIdStr)
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every { storyAccessPort.findScene(storyId, firstSceneIdStr) } returns narrationScene()

            val result = service.execute(command())

            result.scene?.characterOpeningAudio shouldBe null
            result.scene?.characterClosingAudio shouldBe null
            result.scene?.narrationAudio shouldNotBe null
        }
    }
})
