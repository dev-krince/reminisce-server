package com.krince.reminisce.application.service.wordbook

import com.krince.reminisce.application.port.access.child.ChildAccessPort
import com.krince.reminisce.application.port.`in`.wordbook.command.GetStoryWordsCommand
import com.krince.reminisce.application.port.out.tts.NARRATOR_VOICE_PROFILE
import com.krince.reminisce.application.port.out.tts.TtsPort
import com.krince.reminisce.application.port.out.wordbook.LoadStoryWordPort
import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.story.vo.StoryId
import com.krince.reminisce.domain.model.user.vo.UserId
import com.krince.reminisce.domain.model.wordbook.StoryWord
import com.krince.reminisce.domain.model.wordbook.StoryWordGroup
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
import io.mockk.verify

@Tags("test", "unitTest")
@DisplayName("GetStoryWordsApplicationService 단위테스트")
class GetStoryWordsApplicationServiceTest : FunSpec({

    val loadStoryWordPort = mockk<LoadStoryWordPort>()
    val childAccessPort = mockk<ChildAccessPort>()
    val ttsPort = mockk<TtsPort>()
    val service = GetStoryWordsApplicationService(
        loadStoryWordPort = loadStoryWordPort,
        childAccessPort = childAccessPort,
        ttsPort = ttsPort,
    )

    beforeEach {
        clearAllMocks()
    }

    val childIdStr = "child-uuid-1"
    val guardianIdStr = "guardian-uuid-1"
    val childId = ChildId(childIdStr)
    val guardianId = UserId(guardianIdStr)
    val command = GetStoryWordsCommand(childId = childIdStr, guardianId = guardianIdStr)

    fun bangguiGroup(): StoryWordGroup = StoryWordGroup(
        storyId = StoryId("story-1"),
        storyTitle = "방귀 뀌는 며느리",
        words = listOf(
            StoryWord(word = "부끄럽다", meaning = "숨고 싶은 마음", imageUrl = "/files/banggui-word-02.png"),
            StoryWord(word = "사과하다", meaning = "미안한 마음을 전하는 것", imageUrl = null),
        ),
    )

    context("성공") {
        test("소유한 아이면 이야기별 단어 그룹을 단어별 발음 오디오와 함께 반환한다") {
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every { loadStoryWordPort.findAllGroups() } returns listOf(bangguiGroup())
            every { ttsPort.synthesize("부끄럽다", NARRATOR_VOICE_PROFILE) } returns "audio://word-1"
            every { ttsPort.synthesize("사과하다", NARRATOR_VOICE_PROFILE) } returns "audio://word-2"

            val results = service.execute(command)

            results.size shouldBe 1
            results[0].storyId shouldBe "story-1"
            results[0].storyTitle shouldBe "방귀 뀌는 며느리"
            results[0].words.size shouldBe 2
            results[0].words[0].word shouldBe "부끄럽다"
            results[0].words[0].meaning shouldBe "숨고 싶은 마음"
            results[0].words[0].imageUrl shouldBe "/files/banggui-word-02.png"
            results[0].words[0].audioUrl shouldBe "audio://word-1"
            results[0].words[1].imageUrl shouldBe null
            results[0].words[1].audioUrl shouldBe "audio://word-2"
        }

        test("발음 합성이 안 되면 audioUrl 없이 단어를 반환한다") {
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every { loadStoryWordPort.findAllGroups() } returns listOf(bangguiGroup())
            every { ttsPort.synthesize(any(), NARRATOR_VOICE_PROFILE) } returns null

            val results = service.execute(command)

            results[0].words[0].audioUrl shouldBe null
            results[0].words[1].audioUrl shouldBe null
        }
    }

    context("예외케이스") {
        test("아이가 없으면 NOT_FOUND를 던지고 카탈로그를 조회하지 않는다") {
            every { childAccessPort.findGuardianId(childId) } returns null

            val exception = shouldThrow<NotFoundException> { service.execute(command) }

            exception.exceptionResponseCode shouldBe NOT_FOUND
            verify(exactly = 0) { loadStoryWordPort.findAllGroups() }
        }

        test("다른 보호자의 아이면 NOT_FOUND로 은닉한다") {
            every { childAccessPort.findGuardianId(childId) } returns UserId("other-guardian")

            val exception = shouldThrow<NotFoundException> { service.execute(command) }

            exception.exceptionResponseCode shouldBe NOT_FOUND
            verify(exactly = 0) { loadStoryWordPort.findAllGroups() }
        }
    }
})
