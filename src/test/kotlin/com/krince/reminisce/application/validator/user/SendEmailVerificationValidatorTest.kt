package com.krince.reminisce.application.validator.user

import com.krince.reminisce.shared.exception.ConflictException
import com.krince.reminisce.shared.response.ExceptionResponseCode.DUPLICATE_EMAIL
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@Tags("test", "unitTest")
@DisplayName("SendEmailVerificationValidator 단위테스트")
class SendEmailVerificationValidatorTest : FunSpec({

    context("validateNotDuplicated") {
        context("성공") {
            test("이메일이 존재하지 않으면 예외를 던지지 않는다") {
                shouldNotThrowAny { SendEmailVerificationValidator.validateNotDuplicated(exists = false) }
            }
        }
        context("실패") {
            test("이메일이 이미 존재하면 DUPLICATE_EMAIL ConflictException을 던진다") {
                val exception = shouldThrow<ConflictException> {
                    SendEmailVerificationValidator.validateNotDuplicated(exists = true)
                }

                exception.exceptionResponseCode shouldBe DUPLICATE_EMAIL
            }
        }
    }
})
