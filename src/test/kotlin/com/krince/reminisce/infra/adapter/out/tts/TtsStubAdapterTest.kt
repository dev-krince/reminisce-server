package com.krince.reminisce.infra.adapter.out.tts

import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotBeBlank

@Tags("test", "unitTest")
@DisplayName("TtsStubAdapter 단위테스트")
class TtsStubAdapterTest : FunSpec({

    val adapter = TtsStubAdapter()

    context("synthesize") {
        test("비어있지 않은 텍스트는 non-null·non-blank 참조를 반환한다") {
            val result = adapter.synthesize("며느리가 참 힘들었겠어요")

            result shouldNotBe null
            result!!.shouldNotBeBlank()
        }

        test("같은 텍스트는 항상 동일한 참조를 반환한다") {
            val text = "결정론적 텍스트"

            val first = adapter.synthesize(text)
            val second = adapter.synthesize(text)

            first shouldBe second
        }

        test("빈 문자열은 null을 반환한다") {
            val result = adapter.synthesize("")

            result shouldBe null
        }

        test("공백만 있는 문자열은 null을 반환한다") {
            val result = adapter.synthesize("   ")

            result shouldBe null
        }

        test("앞뒤 공백이 있어도 실제 내용이 있으면 non-null을 반환한다") {
            val result = adapter.synthesize("  텍스트  ")

            result shouldNotBe null
        }
    }
})
