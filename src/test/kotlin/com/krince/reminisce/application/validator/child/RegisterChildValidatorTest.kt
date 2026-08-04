package com.krince.reminisce.application.validator.child

import com.krince.reminisce.shared.exception.BadRequestException
import com.krince.reminisce.shared.exception.BusinessRuleViolationException
import com.krince.reminisce.shared.response.ExceptionResponseCode.CHILD_LIMIT_EXCEEDED
import com.krince.reminisce.shared.response.ExceptionResponseCode.INVALID_BIRTH_YEAR
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@Tags("test", "unitTest")
@DisplayName("RegisterChildValidator 단위테스트")
class RegisterChildValidatorTest : FunSpec({

    val maxPerGuardian = 3
    val currentYear = 2026

    context("validateWithinLimit") {
        context("성공") {
            test("현재 수가 상한보다 작으면 통과한다") {
                shouldNotThrowAny { RegisterChildValidator.validateWithinLimit(0, maxPerGuardian) }
            }

            test("현재 수가 상한 바로 아래이면 통과한다") {
                shouldNotThrowAny {
                    RegisterChildValidator.validateWithinLimit((maxPerGuardian - 1).toLong(), maxPerGuardian)
                }
            }
        }
        context("실패") {
            test("현재 수가 상한과 같으면 CHILD_LIMIT_EXCEEDED를 던진다") {
                val exception = shouldThrow<BusinessRuleViolationException> {
                    RegisterChildValidator.validateWithinLimit(maxPerGuardian.toLong(), maxPerGuardian)
                }

                exception.exceptionResponseCode shouldBe CHILD_LIMIT_EXCEEDED
            }

            test("현재 수가 상한을 초과하면 CHILD_LIMIT_EXCEEDED를 던진다") {
                val exception = shouldThrow<BusinessRuleViolationException> {
                    RegisterChildValidator.validateWithinLimit((maxPerGuardian + 1).toLong(), maxPerGuardian)
                }

                exception.exceptionResponseCode shouldBe CHILD_LIMIT_EXCEEDED
            }
        }
    }

    context("validateBirthYearNotInFuture") {
        context("성공") {
            test("현재연도이면 통과한다") {
                shouldNotThrowAny {
                    RegisterChildValidator.validateBirthYearNotInFuture(currentYear, currentYear)
                }
            }

            test("과거연도이면 통과한다") {
                shouldNotThrowAny {
                    RegisterChildValidator.validateBirthYearNotInFuture(currentYear - 1, currentYear)
                }
            }
        }
        context("실패") {
            test("미래연도이면 INVALID_BIRTH_YEAR를 던진다") {
                val exception = shouldThrow<BadRequestException> {
                    RegisterChildValidator.validateBirthYearNotInFuture(currentYear + 1, currentYear)
                }

                exception.exceptionResponseCode shouldBe INVALID_BIRTH_YEAR
            }
        }
    }
})
