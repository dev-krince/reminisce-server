package com.krince.reminisce.application.service

import com.krince.reminisce.application.port.`in`.child.command.GetChildCommand
import com.krince.reminisce.application.port.`in`.child.command.GetChildrenCommand
import com.krince.reminisce.application.port.`in`.child.command.RegisterChildCommand
import com.krince.reminisce.application.port.out.child.CommandChildPort
import com.krince.reminisce.application.port.out.child.LoadChildPort
import com.krince.reminisce.application.port.out.childconsent.CommandChildConsentPort
import com.krince.reminisce.domain.model.child.Child
import com.krince.reminisce.domain.model.child.vo.BirthYear
import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.child.vo.ChildNickname
import com.krince.reminisce.domain.model.childconsent.ChildConsent
import com.krince.reminisce.domain.model.childconsent.vo.VerificationMethod
import com.krince.reminisce.domain.model.user.vo.UserId
import com.krince.reminisce.infra.config.properties.ChildPolicyProperties
import com.krince.reminisce.shared.exception.BadRequestException
import com.krince.reminisce.shared.exception.BusinessRuleViolationException
import com.krince.reminisce.shared.exception.NotFoundException
import com.krince.reminisce.shared.response.ExceptionResponseCode.CHILD_LIMIT_EXCEEDED
import com.krince.reminisce.shared.response.ExceptionResponseCode.INVALID_BIRTH_YEAR
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND_CHILD
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
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

@Tags("test", "unitTest")
@DisplayName("ChildApplicationService 단위테스트")
class ChildApplicationServiceTest : FunSpec({

    val loadChildPort = mockk<LoadChildPort>()
    val commandChildPort = mockk<CommandChildPort>()
    val commandChildConsentPort = mockk<CommandChildConsentPort>()
    val maxPerGuardian = 3
    val childPolicyProperties = ChildPolicyProperties(maxPerGuardian = maxPerGuardian)
    val fixedYear = 2026
    val fixedInstant = Instant.parse("2026-06-01T00:00:00Z")
    val fixedClock = Clock.fixed(fixedInstant, ZoneOffset.UTC)
    val service = ChildApplicationService(
        loadChildPort = loadChildPort,
        commandChildPort = commandChildPort,
        commandChildConsentPort = commandChildConsentPort,
        childPolicyProperties = childPolicyProperties,
        clock = fixedClock,
    )

    beforeEach { clearAllMocks() }

    val guardianIdStr = "guardian-uuid-1"
    val guardianId = UserId(guardianIdStr)
    val otherGuardianId = UserId("guardian-uuid-2")
    val now = LocalDateTime.now()
    val birthYearValue = 2019
    val consentVersionValue = "v1.0"

    fun child(childIdStr: String, ownerId: UserId, nickname: String): Child = Child(
        childId = ChildId(childIdStr),
        guardianId = ownerId,
        nickname = ChildNickname(nickname),
        birthYear = BirthYear(birthYearValue),
        createdDate = now,
        modifiedDate = now,
    )

    context("RegisterChildUseCase") {
        context("성공") {
            test("상한 미만이면 아이와 보호자 동의를 함께 저장하고 결과를 반환한다") {
                every { loadChildPort.countByGuardianId(guardianId) } returns 0
                val savedSlot = slot<Child>()
                every { commandChildPort.save(capture(savedSlot)) } answers {
                    child(savedSlot.captured.childId.value, savedSlot.captured.guardianId, "토토")
                }
                val consentSlot = slot<ChildConsent>()
                every { commandChildConsentPort.save(capture(consentSlot)) } answers { consentSlot.captured }

                val result = service.execute(
                    RegisterChildCommand(guardianIdStr, "토토", birthYearValue, consentVersionValue),
                )

                result.nickname shouldBe "토토"
                result.birthYear shouldBe birthYearValue
                savedSlot.captured.guardianId shouldBe guardianId
                savedSlot.captured.birthYear shouldBe BirthYear(birthYearValue)
                consentSlot.captured.childId shouldBe savedSlot.captured.childId
                consentSlot.captured.consentVersion.value shouldBe consentVersionValue
                consentSlot.captured.verificationMethod shouldBe VerificationMethod.AUTHENTICATED_PARENT
                consentSlot.captured.withdrawnAt shouldBe null
                consentSlot.captured.consentedAt shouldBe LocalDateTime.now(fixedClock)
                verify(exactly = 1) { commandChildPort.save(any()) }
                verify(exactly = 1) { commandChildConsentPort.save(any()) }
            }
        }
        context("실패") {
            test("현재 수가 상한과 같으면 CHILD_LIMIT_EXCEEDED를 던지고 저장하지 않는다") {
                every { loadChildPort.countByGuardianId(guardianId) } returns maxPerGuardian.toLong()

                val exception = shouldThrow<BusinessRuleViolationException> {
                    service.execute(RegisterChildCommand(guardianIdStr, "토토", birthYearValue, consentVersionValue))
                }

                exception.exceptionResponseCode shouldBe CHILD_LIMIT_EXCEEDED
                verify(exactly = 0) { commandChildPort.save(any()) }
                verify(exactly = 0) { commandChildConsentPort.save(any()) }
            }

            test("미래연도로 등록하면 INVALID_BIRTH_YEAR를 던지고 저장하지 않는다") {
                every { loadChildPort.countByGuardianId(guardianId) } returns 0

                val exception = shouldThrow<BadRequestException> {
                    service.execute(RegisterChildCommand(guardianIdStr, "토토", fixedYear + 1, consentVersionValue))
                }

                exception.exceptionResponseCode shouldBe INVALID_BIRTH_YEAR
                verify(exactly = 0) { commandChildPort.save(any()) }
                verify(exactly = 0) { commandChildConsentPort.save(any()) }
            }
        }
    }

    context("GetChildrenUseCase") {
        context("성공") {
            test("보호자 식별자로만 스코핑된 아이 목록을 반환한다") {
                every { loadChildPort.findAllByGuardianId(guardianId) } returns listOf(
                    child("child-1", guardianId, "토토"),
                    child("child-2", guardianId, "코코"),
                )

                val results = service.execute(GetChildrenCommand(guardianIdStr))

                results shouldHaveSize 2
                verify(exactly = 1) { loadChildPort.findAllByGuardianId(guardianId) }
            }

            test("아이가 없으면 빈 목록을 반환한다") {
                every { loadChildPort.findAllByGuardianId(guardianId) } returns emptyList()

                val results = service.execute(GetChildrenCommand(guardianIdStr))

                results shouldHaveSize 0
            }
        }
    }

    context("GetChildUseCase") {
        context("성공") {
            test("본인 아이면 결과를 반환한다") {
                every { loadChildPort.findById(ChildId("child-1")) } returns child("child-1", guardianId, "토토")

                val result = service.execute(GetChildCommand(guardianIdStr, "child-1"))

                result.childId shouldBe "child-1"
                result.nickname shouldBe "토토"
            }
        }
        context("실패") {
            test("아이가 존재하지 않으면 NOT_FOUND_CHILD를 던진다") {
                every { loadChildPort.findById(ChildId("missing")) } returns null

                val exception = shouldThrow<NotFoundException> {
                    service.execute(GetChildCommand(guardianIdStr, "missing"))
                }

                exception.exceptionResponseCode shouldBe NOT_FOUND_CHILD
            }

            test("타 보호자의 아이면 NOT_FOUND_CHILD로 은닉한다") {
                every { loadChildPort.findById(ChildId("child-1")) } returns child("child-1", otherGuardianId, "토토")

                val exception = shouldThrow<NotFoundException> {
                    service.execute(GetChildCommand(guardianIdStr, "child-1"))
                }

                exception.exceptionResponseCode shouldBe NOT_FOUND_CHILD
            }
        }
    }
})
