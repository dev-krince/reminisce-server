package com.krince.reminisce.domain.model.child.vo

import com.krince.reminisce.shared.exception.BadRequestException
import com.krince.reminisce.shared.response.ExceptionResponseCode.INVALID_CHILD_NICKNAME_LENGTH
import com.krince.reminisce.shared.response.ExceptionResponseCode.REQUIRE_NOT_BLANK
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@Tags("test", "unitTest")
@DisplayName("ChildNickname VO 단위테스트")
class ChildNicknameTest : FunSpec({

    context("생성") {
        context("성공") {
            test("공백이 아니고 길이 범위 안이면 생성된다") {
                val nickname = ChildNickname("토토")

                nickname.value shouldBe "토토"
            }

            test("한 글자 애칭도 허용된다") {
                val nickname = ChildNickname("톳")

                nickname.value shouldBe "톳"
            }

            test("최대 길이 애칭도 허용된다") {
                val maxValue = "가".repeat(20)

                val nickname = ChildNickname(maxValue)

                nickname.value shouldBe maxValue
            }
        }
        context("실패") {
            test("빈 문자열이면 REQUIRE_NOT_BLANK로 BadRequestException을 던진다") {
                val exception = shouldThrow<BadRequestException> { ChildNickname("") }

                exception.exceptionResponseCode shouldBe REQUIRE_NOT_BLANK
            }

            test("공백만 있으면 REQUIRE_NOT_BLANK로 BadRequestException을 던진다") {
                val exception = shouldThrow<BadRequestException> { ChildNickname("   ") }

                exception.exceptionResponseCode shouldBe REQUIRE_NOT_BLANK
            }

            test("최대 길이를 초과하면 INVALID_CHILD_NICKNAME_LENGTH로 BadRequestException을 던진다") {
                val tooLong = "가".repeat(21)

                val exception = shouldThrow<BadRequestException> { ChildNickname(tooLong) }

                exception.exceptionResponseCode shouldBe INVALID_CHILD_NICKNAME_LENGTH
            }
        }
    }
})
