package com.krince.reminisce.application.service.savedstory

import com.krince.reminisce.application.port.access.child.ChildAccessPort
import com.krince.reminisce.application.port.`in`.savedstory.command.AddStoryBookmarkCommand
import com.krince.reminisce.application.port.`in`.savedstory.command.GetBookmarkedStoriesCommand
import com.krince.reminisce.application.port.`in`.savedstory.command.RemoveStoryBookmarkCommand
import com.krince.reminisce.application.port.out.savedstory.CommandSavedStoryPort
import com.krince.reminisce.application.port.out.savedstory.LoadSavedStoryPort
import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.savedstory.SavedStory
import com.krince.reminisce.domain.model.savedstory.vo.SavedStoryId
import com.krince.reminisce.domain.model.story.vo.StoryId
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
import io.mockk.slot
import io.mockk.verify
import java.time.LocalDateTime

@Tags("test", "unitTest")
@DisplayName("StoryBookmarkApplicationService 단위테스트")
class StoryBookmarkApplicationServiceTest : FunSpec({

    val loadSavedStoryPort = mockk<LoadSavedStoryPort>()
    val commandSavedStoryPort = mockk<CommandSavedStoryPort>()
    val childAccessPort = mockk<ChildAccessPort>()
    val service = StoryBookmarkApplicationService(
        loadSavedStoryPort = loadSavedStoryPort,
        commandSavedStoryPort = commandSavedStoryPort,
        childAccessPort = childAccessPort,
    )

    beforeEach { clearAllMocks() }

    val childIdStr = "child-uuid-1"
    val guardianIdStr = "guardian-uuid-1"
    val storyIdStr = "s_banggui_daughter_in_law_001"
    val childId = ChildId(childIdStr)
    val guardianId = UserId(guardianIdStr)
    val storyId = StoryId(storyIdStr)
    val otherGuardianId = UserId("guardian-uuid-2")

    fun addCommand(): AddStoryBookmarkCommand =
        AddStoryBookmarkCommand(childId = childIdStr, guardianId = guardianIdStr, storyId = storyIdStr)

    fun removeCommand(): RemoveStoryBookmarkCommand =
        RemoveStoryBookmarkCommand(childId = childIdStr, guardianId = guardianIdStr, storyId = storyIdStr)

    fun getCommand(): GetBookmarkedStoriesCommand =
        GetBookmarkedStoriesCommand(childId = childIdStr, guardianId = guardianIdStr)

    fun savedStory(savedStoryIdStr: String, storyValue: String): SavedStory = SavedStory(
        savedStoryId = SavedStoryId(savedStoryIdStr),
        childId = childId,
        storyId = StoryId(storyValue),
        createdDate = LocalDateTime.now(),
    )

    context("소유권 실패 은닉") {
        test("찜 추가 시 findGuardianId가 null이면 NotFoundException(NOT_FOUND)을 던지고 저장하지 않는다") {
            every { childAccessPort.findGuardianId(childId) } returns null

            val exception = shouldThrow<NotFoundException> { service.execute(addCommand()) }

            exception.exceptionResponseCode shouldBe NOT_FOUND
            verify(exactly = 0) { commandSavedStoryPort.saveIfAbsent(any()) }
            verify(exactly = 0) { loadSavedStoryPort.findByChildIdAndStoryId(any(), any()) }
        }

        test("찜 추가 시 findGuardianId가 다른 보호자면 NotFoundException(NOT_FOUND)을 던지고 저장하지 않는다") {
            every { childAccessPort.findGuardianId(childId) } returns otherGuardianId

            val exception = shouldThrow<NotFoundException> { service.execute(addCommand()) }

            exception.exceptionResponseCode shouldBe NOT_FOUND
            verify(exactly = 0) { commandSavedStoryPort.saveIfAbsent(any()) }
        }

        test("찜 해제 시 findGuardianId가 다른 보호자면 NotFoundException(NOT_FOUND)을 던지고 삭제하지 않는다") {
            every { childAccessPort.findGuardianId(childId) } returns otherGuardianId

            val exception = shouldThrow<NotFoundException> { service.execute(removeCommand()) }

            exception.exceptionResponseCode shouldBe NOT_FOUND
            verify(exactly = 0) { commandSavedStoryPort.deleteByChildIdAndStoryId(any(), any()) }
        }

        test("목록 조회 시 findGuardianId가 null이면 NotFoundException(NOT_FOUND)을 던지고 조회하지 않는다") {
            every { childAccessPort.findGuardianId(childId) } returns null

            val exception = shouldThrow<NotFoundException> { service.execute(getCommand()) }

            exception.exceptionResponseCode shouldBe NOT_FOUND
            verify(exactly = 0) { loadSavedStoryPort.findAllByChildId(any()) }
        }
    }

    context("찜 추가") {
        test("소유 아이가 처음 찜하면 SavedStory를 생성해 저장하고 결과를 매핑해 반환한다") {
            val persisted = savedStory("saved-story-1", storyIdStr)
            val commandSlot = slot<SavedStory>()
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every { loadSavedStoryPort.findByChildIdAndStoryId(childId, storyId) } returns null
            every { commandSavedStoryPort.saveIfAbsent(capture(commandSlot)) } returns persisted

            val result = service.execute(addCommand())

            result.savedStoryId shouldBe "saved-story-1"
            result.storyId shouldBe storyIdStr
            commandSlot.captured.childId shouldBe childId
            commandSlot.captured.storyId shouldBe storyId
        }

        test("이미 찜한 이야기를 다시 찜하면 저장하지 않고 기존 찜을 반환한다") {
            val existing = savedStory("saved-story-1", storyIdStr)
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every { loadSavedStoryPort.findByChildIdAndStoryId(childId, storyId) } returns existing

            val result = service.execute(addCommand())

            result.savedStoryId shouldBe "saved-story-1"
            result.storyId shouldBe storyIdStr
            verify(exactly = 0) { commandSavedStoryPort.saveIfAbsent(any()) }
        }
    }

    context("찜 해제") {
        test("소유 아이의 찜을 해제하면 childId·storyId로 삭제를 위임한다") {
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every { commandSavedStoryPort.deleteByChildIdAndStoryId(childId, storyId) } returns Unit

            service.execute(removeCommand())

            verify(exactly = 1) { commandSavedStoryPort.deleteByChildIdAndStoryId(childId, storyId) }
        }

        test("찜하지 않은 이야기를 해제해도 삭제 위임만 하고 예외를 던지지 않는다") {
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every { commandSavedStoryPort.deleteByChildIdAndStoryId(childId, storyId) } returns Unit

            service.execute(removeCommand())

            verify(exactly = 1) { commandSavedStoryPort.deleteByChildIdAndStoryId(childId, storyId) }
        }
    }

    context("찜 목록") {
        test("소유 아이의 찜 목록을 그대로 매핑해 반환한다") {
            val recent = savedStory("saved-story-2", "s_two")
            val older = savedStory("saved-story-1", "s_one")
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every { loadSavedStoryPort.findAllByChildId(childId) } returns listOf(recent, older)

            val results = service.execute(getCommand())

            results shouldHaveSize 2
            results[0].savedStoryId shouldBe "saved-story-2"
            results[0].storyId shouldBe "s_two"
            results[1].savedStoryId shouldBe "saved-story-1"
            results[1].storyId shouldBe "s_one"
        }
    }
})
