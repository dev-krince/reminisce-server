package com.krince.reminisce.application.service.wordbook

import com.krince.reminisce.application.port.access.child.ChildAccessPort
import com.krince.reminisce.application.port.`in`.wordbook.command.GetWordbookCommand
import com.krince.reminisce.application.port.`in`.wordbook.command.SaveWordCommand
import com.krince.reminisce.application.port.out.wordbook.CommandSavedWordPort
import com.krince.reminisce.application.port.out.wordbook.LoadSavedWordPort
import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.user.vo.UserId
import com.krince.reminisce.domain.model.wordbook.SavedWord
import com.krince.reminisce.domain.model.wordbook.vo.SavedWordId
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
@DisplayName("WordbookApplicationService 단위테스트")
class WordbookApplicationServiceTest : FunSpec({

    val loadSavedWordPort = mockk<LoadSavedWordPort>()
    val commandSavedWordPort = mockk<CommandSavedWordPort>()
    val childAccessPort = mockk<ChildAccessPort>()
    val service = WordbookApplicationService(
        loadSavedWordPort = loadSavedWordPort,
        commandSavedWordPort = commandSavedWordPort,
        childAccessPort = childAccessPort,
    )

    beforeEach { clearAllMocks() }

    val childIdStr = "child-uuid-1"
    val guardianIdStr = "guardian-uuid-1"
    val childId = ChildId(childIdStr)
    val guardianId = UserId(guardianIdStr)
    val otherGuardianId = UserId("guardian-uuid-2")

    fun saveCommand(): SaveWordCommand = SaveWordCommand(
        childId = childIdStr,
        guardianId = guardianIdStr,
        word = "며느리",
        meaning = "아들의 아내",
        sourceSceneId = "sc-1",
    )

    fun getCommand(): GetWordbookCommand =
        GetWordbookCommand(childId = childIdStr, guardianId = guardianIdStr)

    fun savedWord(savedWordIdStr: String, word: String): SavedWord = SavedWord(
        savedWordId = SavedWordId(savedWordIdStr),
        childId = childId,
        word = word,
        meaning = "뜻-$word",
        sourceSceneId = "sc-1",
        createdDate = LocalDateTime.now(),
    )

    context("소유권 실패") {
        test("저장 시 findGuardianId가 null이면 NotFoundException(NOT_FOUND)을 던지고 저장하지 않는다") {
            every { childAccessPort.findGuardianId(childId) } returns null

            val exception = shouldThrow<NotFoundException> { service.execute(saveCommand()) }

            exception.exceptionResponseCode shouldBe NOT_FOUND
            verify(exactly = 0) { commandSavedWordPort.save(any()) }
        }

        test("저장 시 findGuardianId가 다른 보호자면 NotFoundException(NOT_FOUND)을 던지고 저장하지 않는다") {
            every { childAccessPort.findGuardianId(childId) } returns otherGuardianId

            val exception = shouldThrow<NotFoundException> { service.execute(saveCommand()) }

            exception.exceptionResponseCode shouldBe NOT_FOUND
            verify(exactly = 0) { commandSavedWordPort.save(any()) }
        }

        test("조회 시 findGuardianId가 null이면 NotFoundException(NOT_FOUND)을 던지고 조회하지 않는다") {
            every { childAccessPort.findGuardianId(childId) } returns null

            val exception = shouldThrow<NotFoundException> { service.execute(getCommand()) }

            exception.exceptionResponseCode shouldBe NOT_FOUND
            verify(exactly = 0) { loadSavedWordPort.findAllByChildId(any()) }
        }

        test("조회 시 findGuardianId가 다른 보호자면 NotFoundException(NOT_FOUND)을 던지고 조회하지 않는다") {
            every { childAccessPort.findGuardianId(childId) } returns otherGuardianId

            val exception = shouldThrow<NotFoundException> { service.execute(getCommand()) }

            exception.exceptionResponseCode shouldBe NOT_FOUND
            verify(exactly = 0) { loadSavedWordPort.findAllByChildId(any()) }
        }
    }

    context("성공") {
        test("소유 아이면 단어를 생성해 저장하고 저장 결과를 매핑해 반환한다") {
            val persisted = savedWord("saved-word-1", "며느리")
            val commandSlot = slot<SavedWord>()
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every { commandSavedWordPort.save(capture(commandSlot)) } returns persisted

            val result = service.execute(saveCommand())

            result.savedWordId shouldBe "saved-word-1"
            result.word shouldBe "며느리"
            result.meaning shouldBe "뜻-며느리"
            result.sourceSceneId shouldBe "sc-1"
            commandSlot.captured.childId shouldBe childId
            commandSlot.captured.word shouldBe "며느리"
            commandSlot.captured.meaning shouldBe "아들의 아내"
            commandSlot.captured.sourceSceneId shouldBe "sc-1"
        }

        test("소유 아이면 저장된 단어 목록을 그대로 매핑해 반환한다") {
            val recent = savedWord("saved-word-2", "배나무")
            val older = savedWord("saved-word-1", "며느리")
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every { loadSavedWordPort.findAllByChildId(childId) } returns listOf(recent, older)

            val results = service.execute(getCommand())

            results shouldHaveSize 2
            results[0].savedWordId shouldBe "saved-word-2"
            results[0].word shouldBe "배나무"
            results[1].savedWordId shouldBe "saved-word-1"
            results[1].word shouldBe "며느리"
        }
    }
})
