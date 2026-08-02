package com.krince.reminisce.shared.util

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldMatch
import java.util.UUID

@Tags("test", "unitTest")
@DisplayName("UuidGenerator 단위테스트")
class UuidGeneratorTest : FunSpec({

    context("generate") {
        context("성공") {
            test("호출 시 비어있지 않은 문자열을 반환한다") {
                val result = UuidGenerator.generate()
                result.isNotEmpty() shouldBe true
            }
            test("반환값은 UUID 형식(8-4-4-4-12)을 따른다") {
                val result = UuidGenerator.generate()
                shouldNotThrowAny { UUID.fromString(result) }
                result.shouldMatch(Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"))
            }
            test("여러 번 호출 시 서로 다른 값을 반환한다") {
                val results = (1..10).map { UuidGenerator.generate() }
                results.distinct().shouldHaveSize(10)
            }
        }
    }

    context("generateFileNameFormat") {
        context("성공") {
            test("호출 시 비어있지 않은 문자열을 반환한다") {
                val result = UuidGenerator.generateFileNameFormat()
                result.isNotEmpty() shouldBe true
            }
            test("반환값은 하이픈 없는 64자 hex 문자열이다") {
                val result = UuidGenerator.generateFileNameFormat()
                result.shouldMatch(Regex("^[0-9a-fA-F]{64}$"))
            }
            test("여러 번 호출 시 서로 다른 값을 반환한다") {
                val results = (1..10).map { UuidGenerator.generateFileNameFormat() }
                results.distinct().shouldHaveSize(10)
            }
        }
    }
})
