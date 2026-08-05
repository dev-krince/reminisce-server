package com.krince.reminisce.domain.model.story.vo

import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly

@Tags("test", "unitTest")
@DisplayName("StoryStatus 단위테스트")
class StoryStatusTest : FunSpec({

    context("상태") {
        test("이야기 상태는 DRAFT·PUBLISHED·ARCHIVED 3종만 존재한다") {
            StoryStatus.entries.map { it.name } shouldContainExactly listOf("DRAFT", "PUBLISHED", "ARCHIVED")
        }
    }
})
