package com.krince.reminisce.application.service.speakingsession

import com.krince.reminisce.application.port.access.child.ChildAccessPort
import com.krince.reminisce.application.port.access.story.ResumableStoryDisplayInfo
import com.krince.reminisce.application.port.access.story.StoryAccessPort
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
    val storyAccessPort = mockk<StoryAccessPort>()
    val service = GetResumableSessionsApplicationService(
        loadSpeakingSessionPort = loadSpeakingSessionPort,
        childAccessPort = childAccessPort,
        storyAccessPort = storyAccessPort,
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

    fun displayInfo(
        title: String = "방귀 뀌는 며느리",
        representativeImageUrl: String? = "/files/story-1.png",
        difficulty: String = "보통",
        topics: List<String> = listOf("공감", "존중"),
        currentChapter: Int = 2,
        totalChapters: Int = 3,
    ): ResumableStoryDisplayInfo = ResumableStoryDisplayInfo(
        title = title,
        representativeImageUrl = representativeImageUrl,
        difficulty = difficulty,
        topics = topics,
        currentChapter = currentChapter,
        totalChapters = totalChapters,
    )

    fun session(
        sessionId: String,
        lastActivityAt: LocalDateTime,
        status: SessionStatus = SessionStatus.IN_PROGRESS,
        sessionStoryId: StoryId = storyId,
        currentSceneId: String? = null,
    ): SpeakingSession = SpeakingSession(
        sessionId = SpeakingSessionId(sessionId),
        childId = childId,
        storyId = sessionStoryId,
        status = status,
        currentSceneId = currentSceneId,
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
            every { loadSpeakingSessionPort.findResumableByChild(childId) } returns listOf(recentSession, olderSession)
            every { storyAccessPort.findResumableDisplayInfo(storyId, null) } returns displayInfo()

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

        test("포트가 반환한 IN_PROGRESS·POST_ACTIVITY 세션을 상태 그대로 요약 매핑해 반환한다") {
            val inProgressSession = session("session-in-progress", recentAt, SessionStatus.IN_PROGRESS)
            val postActivitySession = session("session-post-activity", olderAt, SessionStatus.POST_ACTIVITY)
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every {
                loadSpeakingSessionPort.findResumableByChild(childId)
            } returns listOf(inProgressSession, postActivitySession)
            every { storyAccessPort.findResumableDisplayInfo(storyId, null) } returns displayInfo()

            val results = service.execute(command())

            results.size shouldBe 2
            results[0].sessionId shouldBe "session-in-progress"
            results[0].status shouldBe SessionStatus.IN_PROGRESS.name
            results[1].sessionId shouldBe "session-post-activity"
            results[1].status shouldBe SessionStatus.POST_ACTIVITY.name
        }

        test("세션마다 그 세션의 스토리 표시정보를 얹어 반환한다") {
            val session = session("session-1", recentAt, currentSceneId = "scene-1")
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every { loadSpeakingSessionPort.findResumableByChild(childId) } returns listOf(session)
            every { storyAccessPort.findResumableDisplayInfo(storyId, "scene-1") } returns displayInfo(
                title = "방귀 뀌는 며느리",
                representativeImageUrl = "/files/story-1.png",
                difficulty = "보통",
                topics = listOf("공감", "존중"),
                currentChapter = 2,
                totalChapters = 3,
            )

            val results = service.execute(command())

            results[0].title shouldBe "방귀 뀌는 며느리"
            results[0].representativeImageUrl shouldBe "/files/story-1.png"
            results[0].difficulty shouldBe "보통"
            results[0].topics shouldBe listOf("공감", "존중")
            results[0].currentChapter shouldBe 2
            results[0].totalChapters shouldBe 3
        }

        test("서로 다른 storyId를 가진 세션이 섞여도 각 세션에 올바른 스토리 표시정보가 매칭된다") {
            val storyIdA = StoryId("story-A")
            val storyIdB = StoryId("story-B")
            val sessionA = session("session-A", recentAt, sessionStoryId = storyIdA, currentSceneId = "scene-A")
            val sessionB = session("session-B", olderAt, sessionStoryId = storyIdB, currentSceneId = "scene-B")
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every { loadSpeakingSessionPort.findResumableByChild(childId) } returns listOf(sessionA, sessionB)
            every { storyAccessPort.findResumableDisplayInfo(storyIdA, "scene-A") } returns displayInfo(
                title = "이야기 A",
                currentChapter = 1,
                totalChapters = 2,
            )
            every { storyAccessPort.findResumableDisplayInfo(storyIdB, "scene-B") } returns displayInfo(
                title = "이야기 B",
                currentChapter = 3,
                totalChapters = 5,
            )

            val results = service.execute(command())

            results[0].storyId shouldBe "story-A"
            results[0].title shouldBe "이야기 A"
            results[0].currentChapter shouldBe 1
            results[0].totalChapters shouldBe 2
            results[1].storyId shouldBe "story-B"
            results[1].title shouldBe "이야기 B"
            results[1].currentChapter shouldBe 3
            results[1].totalChapters shouldBe 5
        }
    }
})
