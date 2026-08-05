package com.krince.reminisce.infra.adapter.out.persistence.story.converter

import com.krince.reminisce.domain.model.story.vo.ThinkingElement
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe

@Tags("test", "unitTest")
@DisplayName("RequiredElementsConverter 단위테스트")
class RequiredElementsConverterTest : FunSpec({

    val converter = RequiredElementsConverter()

    context("convertToDatabaseColumn") {
        test("사고 요소 목록을 JSON 배열 문자열로 직렬화한다") {
            val requiredElements = listOf(
                ThinkingElement.PERSPECTIVE,
                ThinkingElement.EMOTION,
                ThinkingElement.REASON,
                ThinkingElement.SOLUTION,
            )

            val dbData = converter.convertToDatabaseColumn(requiredElements)

            dbData shouldBe """["PERSPECTIVE","EMOTION","REASON","SOLUTION"]"""
        }

        test("null이면 null을 반환한다") {
            converter.convertToDatabaseColumn(null) shouldBe null
        }

        test("빈 목록이면 빈 JSON 배열 문자열을 반환한다") {
            converter.convertToDatabaseColumn(emptyList()) shouldBe "[]"
        }
    }

    context("convertToEntityAttribute") {
        test("JSON 배열 문자열을 사고 요소 목록으로 복원한다") {
            val restored = converter.convertToEntityAttribute("""["SOLUTION","REASON","REQUEST","RESULT"]""")

            restored shouldContainExactly listOf(
                ThinkingElement.SOLUTION,
                ThinkingElement.REASON,
                ThinkingElement.REQUEST,
                ThinkingElement.RESULT,
            )
        }

        test("null이면 null을 반환한다") {
            converter.convertToEntityAttribute(null) shouldBe null
        }

        test("빈 JSON 배열 문자열이면 빈 목록을 반환한다") {
            converter.convertToEntityAttribute("[]") shouldBe emptyList<ThinkingElement>()
        }
    }

    context("왕복") {
        test("직렬화 후 복원하면 순서까지 같은 목록이 된다") {
            val requiredElements = listOf(
                ThinkingElement.EMOTION,
                ThinkingElement.PERSPECTIVE,
                ThinkingElement.RESULT,
                ThinkingElement.SOLUTION,
            )

            val restored = converter.convertToEntityAttribute(converter.convertToDatabaseColumn(requiredElements))

            restored shouldContainExactly requiredElements
        }
    }
})
