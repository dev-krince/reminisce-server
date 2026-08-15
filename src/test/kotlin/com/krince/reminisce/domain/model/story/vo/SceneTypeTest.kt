package com.krince.reminisce.domain.model.story.vo

import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly

@Tags("test", "unitTest")
@DisplayName("SceneType 단위테스트")
class SceneTypeTest : FunSpec({

    context("종류") {
        test("장면 종류는 전개·캐릭터 대사·대화 3종만 존재한다") {
            SceneType.entries.map { it.name } shouldContainExactly listOf("NARRATION", "CHARACTER_LINE", "DIALOGUE")
        }
    }
})
