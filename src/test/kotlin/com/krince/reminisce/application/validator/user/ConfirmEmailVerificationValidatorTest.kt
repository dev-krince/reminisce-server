package com.krince.reminisce.application.validator.user

import com.krince.reminisce.shared.exception.BadRequestException
import com.krince.reminisce.shared.response.ExceptionResponseCode.EXPIRED_VERIFICATION_CODE
import com.krince.reminisce.shared.response.ExceptionResponseCode.INVALID_VERIFICATION_CODE
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@Tags("test", "unitTest")
@DisplayName("ConfirmEmailVerificationValidator 단위테스트")
class ConfirmEmailVerificationValidatorTest : FunSpec({

    context("validateCodeMatches") {
        context("성공") {
            test("저장된 코드와 입력 코드가 같으면 예외를 던지지 않는다") {
                shouldNotThrowAny {
                    ConfirmEmailVerificationValidator.validateCodeMatches(storedCode = "123456", inputCode = "123456")
                }
            }
        }
        context("실패") {
            test("저장된 코드가 없으면 EXPIRED_VERIFICATION_CODE BadRequestException을 던진다") {
                val exception = shouldThrow<BadRequestException> {
                    ConfirmEmailVerificationValidator.validateCodeMatches(storedCode = null, inputCode = "123456")
                }

                exception.exceptionResponseCode shouldBe EXPIRED_VERIFICATION_CODE
            }
            test("코드가 일치하지 않으면 INVALID_VERIFICATION_CODE BadRequestException을 던진다") {
                val exception = shouldThrow<BadRequestException> {
                    ConfirmEmailVerificationValidator.validateCodeMatches(storedCode = "123456", inputCode = "000000")
                }

                exception.exceptionResponseCode shouldBe INVALID_VERIFICATION_CODE
            }
        }
    }
})
