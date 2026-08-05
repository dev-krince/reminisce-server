package com.krince.reminisce.domain.model.childconsent.vo

import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe

@Tags("test", "unitTest")
@DisplayName("VerificationMethod enum 단위테스트")
class VerificationMethodTest : FunSpec({

    context("값 집합") {
        test("보호자 온라인·기관 서면·모바일 확인 세 가지 확인 방식을 가진다") {
            VerificationMethod.entries.map { it.name }.shouldContainExactlyInAnyOrder(
                "AUTHENTICATED_PARENT",
                "INSTITUTION_PAPER",
                "MOBILE_VERIFICATION",
            )
        }

        test("valueOf로 각 이름을 되돌릴 수 있다") {
            VerificationMethod.valueOf("AUTHENTICATED_PARENT") shouldBe VerificationMethod.AUTHENTICATED_PARENT
            VerificationMethod.valueOf("INSTITUTION_PAPER") shouldBe VerificationMethod.INSTITUTION_PAPER
            VerificationMethod.valueOf("MOBILE_VERIFICATION") shouldBe VerificationMethod.MOBILE_VERIFICATION
        }
    }
})
