package com.krince.reminisce.domain.model.story.vo

import com.krince.reminisce.shared.exception.BadRequestException
import com.krince.reminisce.shared.response.ExceptionResponseCode.REQUIRE_NOT_BLANK
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@Tags("test", "unitTest")
@DisplayName("Difficulty VO 단위테스트")
class DifficultyTest : FunSpec({

    context("생성") {
        context("성공") {
            test("공백이 아닌 값이면 생성된다") {
                val difficulty = Difficulty("보통")

                difficulty.value shouldBe "보통"
            }
        }
        context("실패") {
            test("빈 문자열이면 REQUIRE_NOT_BLANK로 BadRequestException을 던진다") {
                val exception = shouldThrow<BadRequestException> { Difficulty("") }

                exception.exceptionResponseCode shouldBe REQUIRE_NOT_BLANK
            }

            test("공백만 있으면 REQUIRE_NOT_BLANK로 BadRequestException을 던진다") {
                val exception = shouldThrow<BadRequestException> { Difficulty("   ") }

                exception.exceptionResponseCode shouldBe REQUIRE_NOT_BLANK
            }
        }
    }
})
