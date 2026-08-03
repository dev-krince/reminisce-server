package com.krince.reminisce.domain.model.user.vo

import com.krince.reminisce.shared.exception.BadRequestException
import com.krince.reminisce.shared.response.ExceptionResponseCode.REQUIRE_NOT_BLANK
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@Tags("test", "unitTest")
@DisplayName("Password 단위테스트")
class PasswordTest : FunSpec({

    context("생성") {
        context("성공") {
            test("BCrypt 해시 문자열을 그대로 감싼다") {
                val hash = "\$2a\$10\$abcdefghijklmnopqrstuv"
                val password = Password(hash)

                password.value shouldBe hash
            }
        }
        context("실패") {
            test("빈 문자열이면 REQUIRE_NOT_BLANK BadRequestException을 던진다") {
                val exception = shouldThrow<BadRequestException> { Password("") }

                exception.exceptionResponseCode shouldBe REQUIRE_NOT_BLANK
            }
            test("공백만 있으면 REQUIRE_NOT_BLANK BadRequestException을 던진다") {
                val exception = shouldThrow<BadRequestException> { Password("   ") }

                exception.exceptionResponseCode shouldBe REQUIRE_NOT_BLANK
            }
        }
    }
})
