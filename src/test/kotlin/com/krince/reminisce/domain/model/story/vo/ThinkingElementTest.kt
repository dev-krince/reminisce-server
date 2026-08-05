package com.krince.reminisce.domain.model.story.vo

import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly

@Tags("test", "unitTest")
@DisplayName("ThinkingElement 단위테스트")
class ThinkingElementTest : FunSpec({

    context("캐논") {
        test("사고 요소는 캐논 8종만 존재하고 EXPRESSION 같은 오기는 없다") {
            ThinkingElement.entries.map { it.name } shouldContainExactly listOf(
                "DECISION",
                "REASON",
                "PERSPECTIVE",
                "SOLUTION",
                "RESULT",
                "EMOTION",
                "EMPATHY",
                "REQUEST",
            )
        }
    }
})
