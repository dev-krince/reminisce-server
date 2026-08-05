package com.krince.reminisce.domain.model.story.vo

import com.krince.reminisce.shared.exception.BadRequestException
import com.krince.reminisce.shared.response.ExceptionResponseCode.REQUIRE_NOT_BLANK
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@Tags("test", "unitTest")
@DisplayName("SceneId VO 단위테스트")
class SceneIdTest : FunSpec({

    context("생성") {
        context("성공") {
            test("공백이 아닌 값이면 생성된다") {
                val sceneId = SceneId("sc_banggui_01")

                sceneId.value shouldBe "sc_banggui_01"
            }
        }
        context("실패") {
            test("빈 문자열이면 REQUIRE_NOT_BLANK로 BadRequestException을 던진다") {
                val exception = shouldThrow<BadRequestException> { SceneId("") }

                exception.exceptionResponseCode shouldBe REQUIRE_NOT_BLANK
            }

            test("공백만 있으면 REQUIRE_NOT_BLANK로 BadRequestException을 던진다") {
                val exception = shouldThrow<BadRequestException> { SceneId("   ") }

                exception.exceptionResponseCode shouldBe REQUIRE_NOT_BLANK
            }
        }
    }
})
