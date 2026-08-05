package com.krince.reminisce.domain.model.childconsent

import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.childconsent.vo.ConsentVersion
import com.krince.reminisce.domain.model.childconsent.vo.VerificationMethod
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDateTime

@Tags("test", "unitTest")
@DisplayName("ChildConsent 도메인 단위테스트")
class ChildConsentTest : FunSpec({

    val childId = ChildId("child-uuid-1")
    val consentVersion = ConsentVersion("v1.0")
    val consentedAt = LocalDateTime.of(2026, 6, 1, 0, 0)

    context("givenByAuthenticatedParent") {
        context("성공") {
            test("보호자 온라인 동의는 AUTHENTICATED_PARENT로 생성되고 철회되지 않은 상태다") {
                val consent = ChildConsent.givenByAuthenticatedParent(childId, consentVersion, consentedAt)

                consent.verificationMethod shouldBe VerificationMethod.AUTHENTICATED_PARENT
                consent.withdrawnAt shouldBe null
            }

            test("전달한 childId·버전·동의시각을 그대로 담고 식별자를 발급한다") {
                val consent = ChildConsent.givenByAuthenticatedParent(childId, consentVersion, consentedAt)

                consent.childId shouldBe childId
                consent.consentVersion shouldBe consentVersion
                consent.consentedAt shouldBe consentedAt
                consent.consentId.value.isNotBlank() shouldBe true
            }

            test("호출마다 서로 다른 동의 식별자를 발급한다") {
                val first = ChildConsent.givenByAuthenticatedParent(childId, consentVersion, consentedAt)
                val second = ChildConsent.givenByAuthenticatedParent(childId, consentVersion, consentedAt)

                (first.consentId.value == second.consentId.value) shouldBe false
            }
        }
    }
})
