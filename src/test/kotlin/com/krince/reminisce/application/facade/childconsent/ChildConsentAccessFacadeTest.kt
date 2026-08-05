package com.krince.reminisce.application.facade.childconsent

import com.krince.reminisce.application.port.out.childconsent.LoadChildConsentPort
import com.krince.reminisce.domain.model.child.vo.ChildId
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

@Tags("test", "unitTest")
@DisplayName("ChildConsentAccessFacade 단위테스트")
class ChildConsentAccessFacadeTest : FunSpec({

    val loadChildConsentPort = mockk<LoadChildConsentPort>()
    val facade = ChildConsentAccessFacade(loadChildConsentPort)

    val childId = ChildId("child-uuid-1")

    context("hasActiveConsent") {
        context("성공") {
            test("유효 동의가 존재하면 true를 반환한다") {
                clearMocks(loadChildConsentPort)
                every { loadChildConsentPort.existsActiveByChildId(childId) } returns true

                val result = facade.hasActiveConsent(childId)

                result shouldBe true
                verify(exactly = 1) { loadChildConsentPort.existsActiveByChildId(childId) }
            }

            test("유효 동의가 없으면 false를 반환한다") {
                clearMocks(loadChildConsentPort)
                every { loadChildConsentPort.existsActiveByChildId(childId) } returns false

                val result = facade.hasActiveConsent(childId)

                result shouldBe false
                verify(exactly = 1) { loadChildConsentPort.existsActiveByChildId(childId) }
            }
        }
    }
})
