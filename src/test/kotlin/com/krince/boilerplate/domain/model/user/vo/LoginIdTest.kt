package com.krince.boilerplate.domain.model.user.vo

import com.krince.boilerplate.shared.exception.BadRequestException
import com.krince.boilerplate.shared.response.ExceptionResponseCode.REQUIRE_NOT_BLANK
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeBlank

@Tags("test", "unitTest")
@DisplayName("LoginId 단위테스트")
class LoginIdTest : FunSpec({

    context("생성") {
        context("성공") {
            test("비공백 한 글자면 생성되고 value가 보존된다") {
                val vo = LoginId("a")

                vo.value shouldBe "a"
            }
            test("영문과 숫자 조합이면 생성되고 value가 보존된다") {
                val vo = LoginId("testUser1")

                vo.value shouldBe "testUser1"
            }
            test("앞뒤에 공백이 있어도 비공백 문자가 있으면 생성된다") {
                val vo = LoginId("  id  ")

                vo.value shouldBe "  id  "
                vo.value.shouldNotBeBlank()
            }
            test("동일 value로 생성한 두 인스턴스는 같다") {
                val a = LoginId("same")
                val b = LoginId("same")

                a shouldBe b
                a.value shouldBe b.value
            }
            test("서로 다른 value면 다른 인스턴스다") {
                val a = LoginId("user1")
                val b = LoginId("user2")

                a shouldBe a
                (a == b) shouldBe false
            }
        }
        context("실패") {
            test("빈 문자열이면 REQUIRE_NOT_BLANK BadRequestException을 던진다") {
                val ex = shouldThrow<BadRequestException> { LoginId("") }

                ex.exceptionResponseCode shouldBe REQUIRE_NOT_BLANK
                ex.message shouldBe REQUIRE_NOT_BLANK.message
            }
            test("공백만 있으면 REQUIRE_NOT_BLANK BadRequestException을 던진다") {
                val ex = shouldThrow<BadRequestException> { LoginId("   ") }

                ex.exceptionResponseCode shouldBe REQUIRE_NOT_BLANK
            }
            test("탭만 있으면 REQUIRE_NOT_BLANK BadRequestException을 던진다") {
                val ex = shouldThrow<BadRequestException> { LoginId("\t") }

                ex.exceptionResponseCode shouldBe REQUIRE_NOT_BLANK
            }
            test("개행만 있으면 REQUIRE_NOT_BLANK BadRequestException을 던진다") {
                val ex = shouldThrow<BadRequestException> { LoginId("\n") }

                ex.exceptionResponseCode shouldBe REQUIRE_NOT_BLANK
            }
        }
    }
})
