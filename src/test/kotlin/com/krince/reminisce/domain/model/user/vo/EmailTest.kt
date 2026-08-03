package com.krince.reminisce.domain.model.user.vo

import com.krince.reminisce.shared.exception.BadRequestException
import com.krince.reminisce.shared.response.ExceptionResponseCode.INVALID_EMAIL_FORMAT
import com.krince.reminisce.shared.response.ExceptionResponseCode.REQUIRE_NOT_BLANK
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@Tags("test", "unitTest")
@DisplayName("Email 단위테스트")
class EmailTest : FunSpec({

    context("생성") {
        context("성공") {
            test("표준 형식이면 생성되고 value가 보존된다") {
                val email = Email("user@example.com")

                email.value shouldBe "user@example.com"
            }
            test("숫자와 점, 하이픈을 포함한 도메인도 허용한다") {
                val email = Email("first.last+tag@sub-domain.co.kr")

                email.value shouldBe "first.last+tag@sub-domain.co.kr"
            }
            test("동일 value로 생성한 두 인스턴스는 같다") {
                val first = Email("same@example.com")
                val second = Email("same@example.com")

                first shouldBe second
            }
        }
        context("실패") {
            test("빈 문자열이면 REQUIRE_NOT_BLANK BadRequestException을 던진다") {
                val exception = shouldThrow<BadRequestException> { Email("") }

                exception.exceptionResponseCode shouldBe REQUIRE_NOT_BLANK
            }
            test("공백만 있으면 REQUIRE_NOT_BLANK BadRequestException을 던진다") {
                val exception = shouldThrow<BadRequestException> { Email("   ") }

                exception.exceptionResponseCode shouldBe REQUIRE_NOT_BLANK
            }
            test("@가 없으면 INVALID_EMAIL_FORMAT BadRequestException을 던진다") {
                val exception = shouldThrow<BadRequestException> { Email("userexample.com") }

                exception.exceptionResponseCode shouldBe INVALID_EMAIL_FORMAT
            }
            test("도메인 TLD가 없으면 INVALID_EMAIL_FORMAT BadRequestException을 던진다") {
                val exception = shouldThrow<BadRequestException> { Email("user@example") }

                exception.exceptionResponseCode shouldBe INVALID_EMAIL_FORMAT
            }
            test("로컬 파트가 없으면 INVALID_EMAIL_FORMAT BadRequestException을 던진다") {
                val exception = shouldThrow<BadRequestException> { Email("@example.com") }

                exception.exceptionResponseCode shouldBe INVALID_EMAIL_FORMAT
            }
            test("공백이 섞이면 INVALID_EMAIL_FORMAT BadRequestException을 던진다") {
                val exception = shouldThrow<BadRequestException> { Email("us er@example.com") }

                exception.exceptionResponseCode shouldBe INVALID_EMAIL_FORMAT
            }
        }
    }
})
