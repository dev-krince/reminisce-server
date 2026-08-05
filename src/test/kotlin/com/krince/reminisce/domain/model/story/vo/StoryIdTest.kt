package com.krince.reminisce.domain.model.story.vo

import com.krince.reminisce.shared.exception.BadRequestException
import com.krince.reminisce.shared.response.ExceptionResponseCode.REQUIRE_NOT_BLANK
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@Tags("test", "unitTest")
@DisplayName("StoryId VO 단위테스트")
class StoryIdTest : FunSpec({

    context("생성") {
        context("성공") {
            test("공백이 아닌 값이면 생성된다") {
                val storyId = StoryId("s_banggui_daughter_in_law_001")

                storyId.value shouldBe "s_banggui_daughter_in_law_001"
            }
        }
        context("실패") {
            test("빈 문자열이면 REQUIRE_NOT_BLANK로 BadRequestException을 던진다") {
                val exception = shouldThrow<BadRequestException> { StoryId("") }

                exception.exceptionResponseCode shouldBe REQUIRE_NOT_BLANK
            }

            test("공백만 있으면 REQUIRE_NOT_BLANK로 BadRequestException을 던진다") {
                val exception = shouldThrow<BadRequestException> { StoryId("   ") }

                exception.exceptionResponseCode shouldBe REQUIRE_NOT_BLANK
            }
        }
    }
})
