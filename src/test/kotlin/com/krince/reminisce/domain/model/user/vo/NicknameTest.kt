package com.krince.reminisce.domain.model.user.vo

import com.krince.reminisce.shared.exception.BadRequestException
import com.krince.reminisce.shared.response.ExceptionResponseCode.INVALID_NICKNAME_LENGTH
import com.krince.reminisce.shared.response.ExceptionResponseCode.REQUIRE_NOT_BLANK
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@Tags("test", "unitTest")
@DisplayName("Nickname 단위테스트")
class NicknameTest : FunSpec({

    context("생성") {
        context("성공") {
            test("최소 길이 2자면 생성된다") {
                val nickname = Nickname("ab")

                nickname.value shouldBe "ab"
            }
            test("최대 길이 20자면 생성된다") {
                val value = "a".repeat(20)
                val nickname = Nickname(value)

                nickname.value shouldBe value
            }
            test("한글 닉네임도 생성된다") {
                val nickname = Nickname("홍길동")

                nickname.value shouldBe "홍길동"
            }
        }
        context("실패") {
            test("빈 문자열이면 REQUIRE_NOT_BLANK BadRequestException을 던진다") {
                val exception = shouldThrow<BadRequestException> { Nickname("") }

                exception.exceptionResponseCode shouldBe REQUIRE_NOT_BLANK
            }
            test("공백만 있으면 REQUIRE_NOT_BLANK BadRequestException을 던진다") {
                val exception = shouldThrow<BadRequestException> { Nickname("   ") }

                exception.exceptionResponseCode shouldBe REQUIRE_NOT_BLANK
            }
            test("길이가 1자면 INVALID_NICKNAME_LENGTH BadRequestException을 던진다") {
                val exception = shouldThrow<BadRequestException> { Nickname("a") }

                exception.exceptionResponseCode shouldBe INVALID_NICKNAME_LENGTH
            }
            test("길이가 21자면 INVALID_NICKNAME_LENGTH BadRequestException을 던진다") {
                val exception = shouldThrow<BadRequestException> { Nickname("a".repeat(21)) }

                exception.exceptionResponseCode shouldBe INVALID_NICKNAME_LENGTH
            }
        }
    }
})
