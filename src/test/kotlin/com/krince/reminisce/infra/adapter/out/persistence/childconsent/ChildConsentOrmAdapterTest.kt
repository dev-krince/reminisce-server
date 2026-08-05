package com.krince.reminisce.infra.adapter.out.persistence.childconsent

import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.childconsent.ChildConsent
import com.krince.reminisce.domain.model.childconsent.vo.ConsentId
import com.krince.reminisce.domain.model.childconsent.vo.ConsentVersion
import com.krince.reminisce.domain.model.childconsent.vo.VerificationMethod
import com.krince.reminisce.infra.adapter.out.persistence.childconsent.entity.ChildConsentOrmEntity
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.time.LocalDateTime

@Tags("test", "unitTest")
@DisplayName("ChildConsentOrmAdapter 단위테스트")
class ChildConsentOrmAdapterTest : FunSpec({

    val repository = mockk<ChildConsentRepository>()
    val adapter = ChildConsentOrmAdapter(repository)

    val consentIdStr = "consent-uuid-1"
    val childIdStr = "child-uuid-1"
    val consentVersionStr = "v1.0"
    val consentedAt = LocalDateTime.of(2026, 6, 1, 0, 0)

    context("save") {
        context("성공") {
            test("동의 도메인을 엔티티로 저장하고 저장된 값을 도메인으로 되돌린다") {
                clearMocks(repository)
                val entitySlot = slot<ChildConsentOrmEntity>()
                every { repository.saveAndFlush(capture(entitySlot)) } answers { entitySlot.captured }
                val consent = ChildConsent(
                    consentId = ConsentId(consentIdStr),
                    childId = ChildId(childIdStr),
                    consentVersion = ConsentVersion(consentVersionStr),
                    verificationMethod = VerificationMethod.AUTHENTICATED_PARENT,
                    consentedAt = consentedAt,
                )

                val result = adapter.save(consent)

                result.consentId.value shouldBe consentIdStr
                result.childId.value shouldBe childIdStr
                result.consentVersion.value shouldBe consentVersionStr
                result.verificationMethod shouldBe VerificationMethod.AUTHENTICATED_PARENT
                result.withdrawnAt shouldBe null
                entitySlot.captured.childId shouldBe childIdStr
                entitySlot.captured.consentVersion shouldBe consentVersionStr
                entitySlot.captured.verificationMethod shouldBe "AUTHENTICATED_PARENT"
                entitySlot.captured.consentedAt shouldBe consentedAt
                entitySlot.captured.withdrawnAt shouldBe null
                verify(exactly = 1) { repository.saveAndFlush(any()) }
            }
        }
    }

    context("existsActiveByChildId") {
        context("성공") {
            test("철회되지 않은 동의 존재 여부 조회에 위임한다") {
                clearMocks(repository)
                every { repository.existsByChildIdAndWithdrawnAtIsNull(childIdStr) } returns true

                val result = adapter.existsActiveByChildId(ChildId(childIdStr))

                result shouldBe true
                verify(exactly = 1) { repository.existsByChildIdAndWithdrawnAtIsNull(childIdStr) }
            }

            test("유효 동의가 없으면 false를 그대로 반환한다") {
                clearMocks(repository)
                every { repository.existsByChildIdAndWithdrawnAtIsNull(childIdStr) } returns false

                val result = adapter.existsActiveByChildId(ChildId(childIdStr))

                result shouldBe false
                verify(exactly = 1) { repository.existsByChildIdAndWithdrawnAtIsNull(childIdStr) }
            }
        }
    }
})
