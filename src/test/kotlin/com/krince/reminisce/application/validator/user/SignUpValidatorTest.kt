package com.krince.reminisce.application.validator.user

import com.krince.reminisce.shared.exception.BadRequestException
import com.krince.reminisce.shared.exception.ConflictException
import com.krince.reminisce.shared.response.ExceptionResponseCode.DUPLICATE_EMAIL
import com.krince.reminisce.shared.response.ExceptionResponseCode.EMAIL_NOT_VERIFIED
import com.krince.reminisce.shared.response.ExceptionResponseCode.INVALID_PASSWORD_FORMAT
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@Tags("test", "unitTest")
@DisplayName("SignUpValidator 단위테스트")
class SignUpValidatorTest : FunSpec({

    context("validateVerified") {
        context("성공") {
            test("인증 완료면 예외를 던지지 않는다") {
                shouldNotThrowAny { SignUpValidator.validateVerified(isVerified = true) }
            }
        }
        context("실패") {
            test("인증 미완료면 EMAIL_NOT_VERIFIED BadRequestException을 던진다") {
                val exception = shouldThrow<BadRequestException> { SignUpValidator.validateVerified(isVerified = false) }

                exception.exceptionResponseCode shouldBe EMAIL_NOT_VERIFIED
            }
        }
    }

    context("validatePasswordFormat") {
        context("성공") {
            test("8자 이상이며 영문, 숫자, 특수문자를 포함하면 통과한다") {
                shouldNotThrowAny { SignUpValidator.validatePasswordFormat("Password1!") }
            }
            test("정확히 8자이며 조건을 충족하면 통과한다") {
                shouldNotThrowAny { SignUpValidator.validatePasswordFormat("Pass1!aa") }
            }
        }
        context("실패") {
            test("8자 미만이면 INVALID_PASSWORD_FORMAT을 던진다") {
                val exception = shouldThrow<BadRequestException> { SignUpValidator.validatePasswordFormat("Pw1!aaa") }

                exception.exceptionResponseCode shouldBe INVALID_PASSWORD_FORMAT
            }
            test("특수문자가 없으면 INVALID_PASSWORD_FORMAT을 던진다") {
                val exception = shouldThrow<BadRequestException> { SignUpValidator.validatePasswordFormat("Password1") }

                exception.exceptionResponseCode shouldBe INVALID_PASSWORD_FORMAT
            }
            test("숫자가 없으면 INVALID_PASSWORD_FORMAT을 던진다") {
                val exception = shouldThrow<BadRequestException> { SignUpValidator.validatePasswordFormat("Password!") }

                exception.exceptionResponseCode shouldBe INVALID_PASSWORD_FORMAT
            }
            test("영문이 없으면 INVALID_PASSWORD_FORMAT을 던진다") {
                val exception = shouldThrow<BadRequestException> { SignUpValidator.validatePasswordFormat("12345678!") }

                exception.exceptionResponseCode shouldBe INVALID_PASSWORD_FORMAT
            }
        }
    }

    context("validateNotDuplicated") {
        context("성공") {
            test("이메일이 존재하지 않으면 예외를 던지지 않는다") {
                shouldNotThrowAny { SignUpValidator.validateNotDuplicated(exists = false) }
            }
        }
        context("실패") {
            test("이메일이 이미 존재하면 DUPLICATE_EMAIL ConflictException을 던진다") {
                val exception = shouldThrow<ConflictException> { SignUpValidator.validateNotDuplicated(exists = true) }

                exception.exceptionResponseCode shouldBe DUPLICATE_EMAIL
            }
        }
    }
})
