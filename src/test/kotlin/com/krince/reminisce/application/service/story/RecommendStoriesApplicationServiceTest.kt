package com.krince.reminisce.application.service.story

import com.krince.reminisce.application.port.access.child.ChildAccessPort
import com.krince.reminisce.application.port.access.speakingsession.SpeakingSessionAccessPort
import com.krince.reminisce.application.port.`in`.story.command.GetRecommendedStoriesCommand
import com.krince.reminisce.application.port.out.story.LoadStoryPort
import com.krince.reminisce.application.port.out.storyprofile.LoadStoryProfilePort
import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.profileinterview.vo.ProfileInterviewId
import com.krince.reminisce.domain.model.story.Story
import com.krince.reminisce.domain.model.story.vo.Difficulty
import com.krince.reminisce.domain.model.story.vo.StoryId
import com.krince.reminisce.domain.model.story.vo.StoryStatus
import com.krince.reminisce.domain.model.storyprofile.InterestTopic
import com.krince.reminisce.domain.model.storyprofile.StoryProfile
import com.krince.reminisce.domain.model.user.vo.UserId
import com.krince.reminisce.shared.exception.NotFoundException
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk

@Tags("test", "unitTest")
@DisplayName("RecommendStoriesApplicationService 단위테스트")
class RecommendStoriesApplicationServiceTest : FunSpec({

    val loadStoryPort = mockk<LoadStoryPort>()
    val speakingSessionAccessPort = mockk<SpeakingSessionAccessPort>()
    val childAccessPort = mockk<ChildAccessPort>()
    val loadStoryProfilePort = mockk<LoadStoryProfilePort>()
    val service = RecommendStoriesApplicationService(
        loadStoryPort = loadStoryPort,
        speakingSessionAccessPort = speakingSessionAccessPort,
        childAccessPort = childAccessPort,
        loadStoryProfilePort = loadStoryProfilePort,
    )

    beforeEach {
        clearAllMocks()
        every { loadStoryProfilePort.findByChild(any()) } returns null
    }

    val childIdStr = "child-uuid-1"
    val guardianIdStr = "guardian-uuid-1"
    val childId = ChildId(childIdStr)
    val guardianId = UserId(guardianIdStr)
    val otherGuardianId = UserId("guardian-uuid-2")

    fun command(): GetRecommendedStoriesCommand =
        GetRecommendedStoriesCommand(childId = childIdStr, guardianId = guardianIdStr)

    fun story(storyIdStr: String, difficulty: String, topics: List<String> = emptyList()): Story = Story(
        storyId = StoryId(storyIdStr),
        title = "제목-$storyIdStr",
        summary = "요약",
        intro = "도입",
        situation = null,
        childRole = null,
        difficulty = Difficulty(difficulty),
        estimatedMinutes = 20,
        representativeImageUrl = null,
        status = StoryStatus.PUBLISHED,
        postActivityConfig = null,
        topics = topics,
        scenes = emptyList(),
    )

    fun profileWithInterests(interestTopics: List<InterestTopic>): StoryProfile = StoryProfile.create(
        childId = childId,
        interviewId = ProfileInterviewId("interview-1"),
        interestTopics = interestTopics,
        strengths = emptyList(),
        practicePoints = emptyList(),
        speechAnalyses = emptyList(),
        at = java.time.LocalDateTime.of(2026, 8, 17, 12, 0),
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
        test("게시 이야기 중 아이가 시작한 story_id를 제외하고 난이도 오름차순으로 매핑해 반환한다") {
            val startedStoryId = "story-started"
            val storyA = story("story-easy", "가")
            val storyB = story("story-hard", "다")
            val storyC = story(startedStoryId, "나")
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every { speakingSessionAccessPort.findStartedStoryIds(childId) } returns listOf(startedStoryId)
            every { loadStoryPort.findAllPublished() } returns listOf(storyA, storyB, storyC)

            val results = service.execute(command())

            results shouldHaveSize 2
            results[0].storyId shouldBe "story-easy"
            results[1].storyId shouldBe "story-hard"
        }

        test("게시 이야기가 10개를 초과하면 최대 10개만 반환한다") {
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every { speakingSessionAccessPort.findStartedStoryIds(childId) } returns emptyList()
            val stories = (1..12).map { i -> story("story-$i", i.toString().padStart(2, '0')) }
            every { loadStoryPort.findAllPublished() } returns stories

            val results = service.execute(command())

            results shouldHaveSize 10
        }

        test("시작한 이야기가 없으면 전체 게시 이야기를 난이도 오름차순으로 반환한다") {
            val storyA = story("story-a", "나")
            val storyB = story("story-b", "가")
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every { speakingSessionAccessPort.findStartedStoryIds(childId) } returns emptyList()
            every { loadStoryPort.findAllPublished() } returns listOf(storyA, storyB)

            val results = service.execute(command())

            results shouldHaveSize 2
            results[0].storyId shouldBe "story-b"
            results[1].storyId shouldBe "story-a"
        }

        test("모든 이야기를 시작했으면 빈 목록을 반환한다") {
            val storyA = story("story-a", "가")
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every { speakingSessionAccessPort.findStartedStoryIds(childId) } returns listOf("story-a")
            every { loadStoryPort.findAllPublished() } returns listOf(storyA)

            val results = service.execute(command())

            results shouldHaveSize 0
        }
    }

    context("이야기 프로필 반영") {
        test("프로필 관심 태그·카테고리와 topics가 겹치는 이야기를 난이도보다 우선 추천한다") {
            val matchingHard = story("story-match-hard", "다", topics = listOf("관계", "우정"))
            val doubleMatchHard = story("story-double-hard", "다", topics = listOf("동물", "자연"))
            val plainEasy = story("story-plain-easy", "가", topics = listOf("용기"))
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every { speakingSessionAccessPort.findStartedStoryIds(childId) } returns emptyList()
            every { loadStoryPort.findAllPublished() } returns listOf(plainEasy, matchingHard, doubleMatchHard)
            every { loadStoryProfilePort.findByChild(childId) } returns profileWithInterests(
                listOf(
                    InterestTopic(category = "관계", tags = listOf("친구")),
                    InterestTopic(category = "자연", tags = listOf("동물")),
                ),
            )

            val results = service.execute(command())

            results.map { it.storyId } shouldBe listOf("story-double-hard", "story-match-hard", "story-plain-easy")
        }

        test("프로필이 없으면 기존처럼 난이도 오름차순만 적용한다") {
            val storyA = story("story-a", "나", topics = listOf("관계"))
            val storyB = story("story-b", "가")
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every { speakingSessionAccessPort.findStartedStoryIds(childId) } returns emptyList()
            every { loadStoryPort.findAllPublished() } returns listOf(storyA, storyB)
            every { loadStoryProfilePort.findByChild(childId) } returns null

            val results = service.execute(command())

            results.map { it.storyId } shouldBe listOf("story-b", "story-a")
        }
    }
})
