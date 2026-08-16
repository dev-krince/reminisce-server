package com.krince.reminisce.application.service.child

import com.krince.reminisce.application.port.access.child.ChildAccessPort
import com.krince.reminisce.application.port.`in`.child.command.DeleteChildCommand
import com.krince.reminisce.application.port.out.child.CommandChildPort
import com.krince.reminisce.application.port.out.childconsent.CommandChildConsentPort
import com.krince.reminisce.application.port.out.file.StoreFilePort
import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.user.vo.UserId
import com.krince.reminisce.shared.exception.NotFoundException
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND_CHILD
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder

@Tags("test", "unitTest")
@DisplayName("DeleteChildApplicationService 단위테스트")
class DeleteChildApplicationServiceTest : FunSpec({

    val childAccessPort = mockk<ChildAccessPort>()
    val childLearningDataPurger = mockk<ChildLearningDataPurger>()
    val commandChildConsentPort = mockk<CommandChildConsentPort>()
    val commandChildPort = mockk<CommandChildPort>()
    val storeFilePort = mockk<StoreFilePort>()
    val service = DeleteChildApplicationService(
        childAccessPort = childAccessPort,
        childLearningDataPurger = childLearningDataPurger,
        commandChildConsentPort = commandChildConsentPort,
        commandChildPort = commandChildPort,
        storeFilePort = storeFilePort,
    )

    beforeEach { clearAllMocks() }

    val childIdStr = "child-uuid-1"
    val guardianIdStr = "guardian-uuid-1"
    val childId = ChildId(childIdStr)
    val guardianId = UserId(guardianIdStr)

    fun command(): DeleteChildCommand = DeleteChildCommand(guardianId = guardianIdStr, childId = childIdStr)

    context("성공") {
        test("소유 아이 삭제 시 학습데이터 파기→동의 삭제→프로필 삭제 순으로 지우고 재구성 음성을 삭제한다") {
            val audioUrls = listOf("/files/retelling-1.m4a", "/files/retelling-2.webm")
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every { childLearningDataPurger.purge(listOf(childId)) } returns audioUrls
            every { commandChildConsentPort.deleteAllByChildIds(listOf(childId)) } returns Unit
            every { commandChildPort.deleteById(childId) } returns Unit
            every { storeFilePort.deleteFile(any()) } returns Unit

            service.execute(command())

            verifyOrder {
                childLearningDataPurger.purge(listOf(childId))
                commandChildConsentPort.deleteAllByChildIds(listOf(childId))
                commandChildPort.deleteById(childId)
            }
            verify(exactly = 1) { storeFilePort.deleteFile("/files/retelling-1.m4a") }
            verify(exactly = 1) { storeFilePort.deleteFile("/files/retelling-2.webm") }
        }

        test("파기가 반환한 음성 URL이 없으면 deleteFile을 호출하지 않는다") {
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every { childLearningDataPurger.purge(listOf(childId)) } returns emptyList()
            every { commandChildConsentPort.deleteAllByChildIds(listOf(childId)) } returns Unit
            every { commandChildPort.deleteById(childId) } returns Unit

            service.execute(command())

            verify(exactly = 0) { storeFilePort.deleteFile(any()) }
        }
    }

    context("예외케이스") {
        test("아이 소유자가 다르면 NOT_FOUND_CHILD를 던지고 아무것도 삭제하지 않는다") {
            every { childAccessPort.findGuardianId(childId) } returns UserId("other-guardian")

            val exception = shouldThrow<NotFoundException> { service.execute(command()) }

            exception.exceptionResponseCode shouldBe NOT_FOUND_CHILD
            verify(exactly = 0) { childLearningDataPurger.purge(any()) }
            verify(exactly = 0) { commandChildConsentPort.deleteAllByChildIds(any()) }
            verify(exactly = 0) { commandChildPort.deleteById(any()) }
        }

        test("아이가 존재하지 않으면 NOT_FOUND_CHILD를 던지고 아무것도 삭제하지 않는다") {
            every { childAccessPort.findGuardianId(childId) } returns null

            val exception = shouldThrow<NotFoundException> { service.execute(command()) }

            exception.exceptionResponseCode shouldBe NOT_FOUND_CHILD
            verify(exactly = 0) { childLearningDataPurger.purge(any()) }
            verify(exactly = 0) { commandChildPort.deleteById(any()) }
        }
    }
})
