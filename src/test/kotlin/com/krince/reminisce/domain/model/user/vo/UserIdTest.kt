package com.krince.reminisce.domain.model.user.vo

import com.krince.reminisce.shared.exception.BadRequestException
import com.krince.reminisce.shared.response.ExceptionResponseCode.REQUIRE_NOT_BLANK
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeBlank

@Tags("test", "unitTest")
@DisplayName("UserId 단위테스트")
class UserIdTest : FunSpec({

    context("생성") {
        context("성공") {
            test("비공백 한 글자면 생성되고 value가 보존된다") {
                val vo = UserId("a")

                vo.value shouldBe "a"
            }
            test("UUID 형식 문자열이면 생성되고 value가 보존된다") {
                val vo = UserId("a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d")

                vo.value shouldBe "a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d"
            }
            test("앞뒤에 공백이 있어도 비공백 문자가 있으면 생성된다") {
                val vo = UserId("  id  ")

                vo.value shouldBe "  id  "
                vo.value.shouldNotBeBlank()
            }
            test("동일 value로 생성한 두 인스턴스는 같다") {
                val a = UserId("same-id")
                val b = UserId("same-id")

                a shouldBe b
                a.value shouldBe b.value
            }
            test("서로 다른 value면 다른 인스턴스다") {
                val a = UserId("id1")
                val b = UserId("id2")

                a shouldBe a
                (a == b) shouldBe false
            }
        }
        context("실패") {
            test("빈 문자열이면 REQUIRE_NOT_BLANK BadRequestException을 던진다") {
                val ex = shouldThrow<BadRequestException> { UserId("") }

                ex.exceptionResponseCode shouldBe REQUIRE_NOT_BLANK
                ex.message shouldBe REQUIRE_NOT_BLANK.message
            }
            test("공백만 있으면 REQUIRE_NOT_BLANK BadRequestException을 던진다") {
                val ex = shouldThrow<BadRequestException> { UserId("   ") }

                ex.exceptionResponseCode shouldBe REQUIRE_NOT_BLANK
            }
            test("탭만 있으면 REQUIRE_NOT_BLANK BadRequestException을 던진다") {
                val ex = shouldThrow<BadRequestException> { UserId("\t") }

                ex.exceptionResponseCode shouldBe REQUIRE_NOT_BLANK
            }
            test("개행만 있으면 REQUIRE_NOT_BLANK BadRequestException을 던진다") {
                val ex = shouldThrow<BadRequestException> { UserId("\n") }

                ex.exceptionResponseCode shouldBe REQUIRE_NOT_BLANK
            }
        }
    }
})
