package com.krince.reminisce.application.service.storyprofile

import com.krince.reminisce.application.port.access.child.ChildAccessPort
import com.krince.reminisce.application.port.`in`.storyprofile.command.GetStoryProfileCommand
import com.krince.reminisce.application.port.out.profileinterview.LoadInterviewMessagePort
import com.krince.reminisce.application.port.out.profileinterview.LoadProfileInterviewPort
import com.krince.reminisce.application.port.out.storyprofile.CommandStoryProfilePort
import com.krince.reminisce.application.port.out.storyprofile.LoadStoryProfilePort
import com.krince.reminisce.application.port.out.storyprofile.ProfileAnalysisContext
import com.krince.reminisce.application.port.out.storyprofile.ProfileAnalysisPort
import com.krince.reminisce.application.port.out.storyprofile.ProfileAnalysisReport
import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.profileinterview.InterviewMessage
import com.krince.reminisce.domain.model.profileinterview.ProfileInterview
import com.krince.reminisce.domain.model.storyprofile.InterestTopic
import com.krince.reminisce.domain.model.storyprofile.ProfileFinding
import com.krince.reminisce.domain.model.storyprofile.SpeechAreaAnalysis
import com.krince.reminisce.domain.model.storyprofile.StoryProfile
import com.krince.reminisce.domain.model.user.vo.UserId
import com.krince.reminisce.shared.exception.NotFoundException
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND_CHILD
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
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

@Tags("test", "unitTest")
@DisplayName("GetStoryProfileApplicationService 단위테스트")
class GetStoryProfileApplicationServiceTest : FunSpec({

    val childAccessPort = mockk<ChildAccessPort>()
    val loadStoryProfilePort = mockk<LoadStoryProfilePort>()
    val commandStoryProfilePort = mockk<CommandStoryProfilePort>()
    val loadProfileInterviewPort = mockk<LoadProfileInterviewPort>()
    val loadInterviewMessagePort = mockk<LoadInterviewMessagePort>()
    val profileAnalysisPort = mockk<ProfileAnalysisPort>()
    val fixedInstant = LocalDateTime.of(2026, 8, 17, 12, 0).toInstant(ZoneOffset.UTC)
    val clock = Clock.fixed(fixedInstant, ZoneId.of("UTC"))
    val service = GetStoryProfileApplicationService(
        childAccessPort = childAccessPort,
        loadStoryProfilePort = loadStoryProfilePort,
        commandStoryProfilePort = commandStoryProfilePort,
        loadProfileInterviewPort = loadProfileInterviewPort,
        loadInterviewMessagePort = loadInterviewMessagePort,
        profileAnalysisPort = profileAnalysisPort,
        clock = clock,
    )

    beforeEach { clearAllMocks() }

    val childIdStr = "child-uuid-1"
    val guardianIdStr = "guardian-uuid-1"
    val childId = ChildId(childIdStr)
    val guardianId = UserId(guardianIdStr)
    val startedAt = LocalDateTime.of(2026, 8, 17, 10, 0)

    fun command(): GetStoryProfileCommand = GetStoryProfileCommand(guardianId = guardianIdStr, childId = childIdStr)

    fun report(): ProfileAnalysisReport = ProfileAnalysisReport(
        interestTopics = listOf(InterestTopic(category = "관계", tags = listOf("친구", "토끼"))),
        strengths = listOf(ProfileFinding(title = "생각을 표현해요", description = "감정을 말할 수 있어요.")),
        practicePoints = listOf(ProfileFinding(title = "순서대로 이야기하기", description = "연습하면 늘어요.")),
        speechAnalyses = listOf(
            SpeechAreaAnalysis(
                area = "어휘",
                summary = "요약",
                keywords = listOf("귀엽다"),
                feature = "특징",
                evidenceUtterance = "귀여워서요.",
                strength = "잘한 점",
                improvement = "연습할 점",
            ),
        ),
    )

    context("성공") {
        test("저장된 프로필이 있으면 분석 없이 그대로 반환한다") {
            val interview = ProfileInterview.start(childId, startedAt)
            val stored = StoryProfile.create(
                childId = childId,
                interviewId = interview.interviewId,
                interestTopics = report().interestTopics,
                strengths = report().strengths,
                practicePoints = report().practicePoints,
                speechAnalyses = report().speechAnalyses,
                at = startedAt,
            )
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every { loadStoryProfilePort.findByChild(childId) } returns stored

            val result = service.execute(command())

            result.childId shouldBe childIdStr
            result.interestTopics.first().tags shouldContainExactly listOf("친구", "토끼")
            verify(exactly = 0) { profileAnalysisPort.analyze(any()) }
            verify(exactly = 0) { commandStoryProfilePort.save(any()) }
        }

        test("프로필이 없고 완료된 인터뷰가 있으면 대화 전체를 분석해 프로필을 만들어 저장·반환한다") {
            val interview = ProfileInterview.start(childId, startedAt).complete(startedAt.plusMinutes(3))
            val messages = listOf(
                InterviewMessage.qumiLine(interview.interviewId, 1, "어떤 이야기를 좋아해?", startedAt),
                InterviewMessage.childUtterance(interview.interviewId, 2, "토끼요.", null, startedAt),
            )
            val contextSlot = slot<ProfileAnalysisContext>()
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every { loadStoryProfilePort.findByChild(childId) } returns null
            every { loadProfileInterviewPort.findLatestCompletedByChild(childId) } returns interview
            every { loadInterviewMessagePort.findAllByInterview(interview.interviewId) } returns messages
            every { childAccessPort.findChildName(childId) } returns "민서"
            every { profileAnalysisPort.analyze(capture(contextSlot)) } returns report()
            every { commandStoryProfilePort.save(any()) } answers { firstArg() }

            val result = service.execute(command())

            result.interviewId shouldBe interview.interviewId.value
            result.strengths.first().title shouldBe "생각을 표현해요"
            contextSlot.captured.childName shouldBe "민서"
            contextSlot.captured.turns.map { it.isChild } shouldContainExactly listOf(false, true)
            verify(exactly = 1) { commandStoryProfilePort.save(any()) }
        }
    }

    context("예외케이스") {
        test("완료된 인터뷰가 없으면 NOT_FOUND를 던진다") {
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every { loadStoryProfilePort.findByChild(childId) } returns null
            every { loadProfileInterviewPort.findLatestCompletedByChild(childId) } returns null

            val exception = shouldThrow<NotFoundException> { service.execute(command()) }

            exception.exceptionResponseCode shouldBe NOT_FOUND
            verify(exactly = 0) { profileAnalysisPort.analyze(any()) }
        }

        test("남의 아이면 NOT_FOUND_CHILD로 은닉한다") {
            every { childAccessPort.findGuardianId(childId) } returns UserId("other-guardian")

            val exception = shouldThrow<NotFoundException> { service.execute(command()) }

            exception.exceptionResponseCode shouldBe NOT_FOUND_CHILD
            verify(exactly = 0) { loadStoryProfilePort.findByChild(any()) }
        }
    }
})
