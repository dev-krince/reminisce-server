package com.krince.reminisce.application.service.childconsent

import com.krince.reminisce.application.port.access.child.ChildAccessPort
import com.krince.reminisce.application.port.`in`.childconsent.command.WithdrawChildConsentCommand
import com.krince.reminisce.application.port.out.childconsent.CommandChildConsentPort
import com.krince.reminisce.application.port.out.childconsent.LoadChildConsentPort
import com.krince.reminisce.application.port.out.file.StoreFilePort
import com.krince.reminisce.application.service.child.ChildLearningDataPurger
import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.childconsent.ChildConsent
import com.krince.reminisce.domain.model.childconsent.vo.ConsentVersion
import com.krince.reminisce.domain.model.user.vo.UserId
import com.krince.reminisce.shared.exception.BusinessRuleViolationException
import com.krince.reminisce.shared.exception.NotFoundException
import com.krince.reminisce.shared.response.ExceptionResponseCode.BUSINESS_RULE_VIOLATION
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND_CHILD
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
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
@DisplayName("WithdrawChildConsentApplicationService 단위테스트")
class WithdrawChildConsentApplicationServiceTest : FunSpec({

    val childAccessPort = mockk<ChildAccessPort>()
    val loadChildConsentPort = mockk<LoadChildConsentPort>()
    val commandChildConsentPort = mockk<CommandChildConsentPort>()
    val childLearningDataPurger = mockk<ChildLearningDataPurger>()
    val storeFilePort = mockk<StoreFilePort>()
    val fixedInstant = LocalDateTime.of(2026, 7, 1, 9, 30).toInstant(ZoneOffset.UTC)
    val clock = Clock.fixed(fixedInstant, ZoneId.of("UTC"))
    val service = WithdrawChildConsentApplicationService(
        childAccessPort = childAccessPort,
        loadChildConsentPort = loadChildConsentPort,
        commandChildConsentPort = commandChildConsentPort,
        childLearningDataPurger = childLearningDataPurger,
        storeFilePort = storeFilePort,
        clock = clock,
    )

    beforeEach { clearAllMocks() }

    val childIdStr = "child-uuid-1"
    val guardianIdStr = "guardian-uuid-1"
    val childId = ChildId(childIdStr)
    val guardianId = UserId(guardianIdStr)

    fun activeConsent(): ChildConsent = ChildConsent.givenByAuthenticatedParent(
        childId = childId,
        consentVersion = ConsentVersion("v1.0"),
        consentedAt = LocalDateTime.of(2026, 6, 1, 0, 0),
    )

    fun command(): WithdrawChildConsentCommand =
        WithdrawChildConsentCommand(childId = childIdStr, guardianId = guardianIdStr)

    context("성공") {
        test("소유 아이 철회 시 주입 Clock 시각으로 withdrawnAt을 세팅해 저장하고 파기 후 음성을 삭제한다") {
            val consent = activeConsent()
            val audioUrls = listOf("/files/retelling-1.m4a", "/files/retelling-2.webm")
            val savedSlot = slot<ChildConsent>()
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every { loadChildConsentPort.findActiveByChildId(childId) } returns consent
            every { commandChildConsentPort.save(capture(savedSlot)) } answers { savedSlot.captured }
            every { childLearningDataPurger.purge(listOf(childId)) } returns audioUrls
            every { storeFilePort.deleteFile(any()) } returns Unit

            service.execute(command())

            savedSlot.captured.withdrawnAt shouldBe LocalDateTime.now(clock)
            savedSlot.captured.consentId shouldBe consent.consentId
            verify(exactly = 1) { childLearningDataPurger.purge(listOf(childId)) }
            verify(exactly = 1) { storeFilePort.deleteFile("/files/retelling-1.m4a") }
            verify(exactly = 1) { storeFilePort.deleteFile("/files/retelling-2.webm") }
        }

        test("파기가 반환한 음성 URL이 없으면 deleteFile을 호출하지 않는다") {
            val consent = activeConsent()
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every { loadChildConsentPort.findActiveByChildId(childId) } returns consent
            every { commandChildConsentPort.save(any()) } answers { firstArg() }
            every { childLearningDataPurger.purge(listOf(childId)) } returns emptyList()

            service.execute(command())

            verify(exactly = 0) { storeFilePort.deleteFile(any()) }
        }
    }

    context("예외케이스") {
        test("아이 소유자가 다르면 NOT_FOUND_CHILD를 던지고 파기하지 않는다") {
            every { childAccessPort.findGuardianId(childId) } returns UserId("other-guardian")

            val exception = shouldThrow<NotFoundException> { service.execute(command()) }

            exception.exceptionResponseCode shouldBe NOT_FOUND_CHILD
            verify(exactly = 0) { loadChildConsentPort.findActiveByChildId(any()) }
            verify(exactly = 0) { childLearningDataPurger.purge(any()) }
            verify(exactly = 0) { commandChildConsentPort.save(any()) }
        }

        test("아이가 존재하지 않으면 NOT_FOUND_CHILD를 던지고 파기하지 않는다") {
            every { childAccessPort.findGuardianId(childId) } returns null

            val exception = shouldThrow<NotFoundException> { service.execute(command()) }

            exception.exceptionResponseCode shouldBe NOT_FOUND_CHILD
            verify(exactly = 0) { childLearningDataPurger.purge(any()) }
        }

        test("활성 동의가 없으면 BUSINESS_RULE_VIOLATION을 던지고 파기·저장하지 않는다") {
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every { loadChildConsentPort.findActiveByChildId(childId) } returns null

            val exception = shouldThrow<BusinessRuleViolationException> { service.execute(command()) }

            exception.exceptionResponseCode shouldBe BUSINESS_RULE_VIOLATION
            verify(exactly = 0) { commandChildConsentPort.save(any()) }
            verify(exactly = 0) { childLearningDataPurger.purge(any()) }
        }
    }
})
