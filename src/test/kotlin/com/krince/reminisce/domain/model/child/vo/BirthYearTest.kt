package com.krince.reminisce.domain.model.child.vo

import com.krince.reminisce.shared.exception.BadRequestException
import com.krince.reminisce.shared.response.ExceptionResponseCode.INVALID_BIRTH_YEAR
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@Tags("test", "unitTest")
@DisplayName("BirthYear VO 단위테스트")
class BirthYearTest : FunSpec({

    context("생성") {
        context("성공") {
            test("최소 경계값이면 생성된다") {
                val birthYear = BirthYear(1900)

                birthYear.value shouldBe 1900
            }

            test("최대 경계값이면 생성된다") {
                val birthYear = BirthYear(9999)

                birthYear.value shouldBe 9999
            }

            test("범위 내 값이면 생성된다") {
                val birthYear = BirthYear(2019)

                birthYear.value shouldBe 2019
            }
        }
        context("실패") {
            test("최소 경계 미만이면 INVALID_BIRTH_YEAR로 BadRequestException을 던진다") {
                val exception = shouldThrow<BadRequestException> { BirthYear(1899) }

                exception.exceptionResponseCode shouldBe INVALID_BIRTH_YEAR
            }

            test("최대 경계 초과이면 INVALID_BIRTH_YEAR로 BadRequestException을 던진다") {
                val exception = shouldThrow<BadRequestException> { BirthYear(10000) }

                exception.exceptionResponseCode shouldBe INVALID_BIRTH_YEAR
            }

            test("음수이면 INVALID_BIRTH_YEAR로 BadRequestException을 던진다") {
                val exception = shouldThrow<BadRequestException> { BirthYear(-1) }

                exception.exceptionResponseCode shouldBe INVALID_BIRTH_YEAR
            }

            test("0이면 INVALID_BIRTH_YEAR로 BadRequestException을 던진다") {
                val exception = shouldThrow<BadRequestException> { BirthYear(0) }

                exception.exceptionResponseCode shouldBe INVALID_BIRTH_YEAR
            }
        }
    }
})
