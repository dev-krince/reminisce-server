package com.krince.reminisce.infra.adapter.out.persistence.story.converter

import com.fasterxml.jackson.databind.ObjectMapper
import com.krince.reminisce.domain.model.story.Mission
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

@Tags("test", "unitTest")
@DisplayName("MissionConverter 단위테스트")
class MissionConverterTest : FunSpec({

    val converter = MissionConverter()
    val objectMapper = ObjectMapper()

    fun mission(): Mission = Mission(
        goal = "높은 배나무의 배를 떨어뜨리기 위해 며느리의 방귀를 안전하게 사용할 수 있는 방법 찾기",
        examples = listOf("무엇을 사용할 것인지", "주변 사람들과 시아버지는 어디로 피해야 할지"),
    )

    context("convertToDatabaseColumn") {
        test("DB 정본 JSON 키(goal·examples)로 직렬화한다") {
            val dbData = converter.convertToDatabaseColumn(mission())

            dbData.shouldNotBeNull()
            val rootNode = objectMapper.readTree(dbData)
            rootNode.path("goal").asText() shouldBe "높은 배나무의 배를 떨어뜨리기 위해 며느리의 방귀를 안전하게 사용할 수 있는 방법 찾기"
            rootNode.path("examples").map { it.asText() } shouldBe listOf("무엇을 사용할 것인지", "주변 사람들과 시아버지는 어디로 피해야 할지")
        }

        test("null이면 null을 반환한다") {
            converter.convertToDatabaseColumn(null) shouldBe null
        }
    }

    context("convertToEntityAttribute") {
        test("DB 정본 예시 JSON을 도메인 값 객체로 복원한다") {
            val dbData = """
                {
                  "goal": "높은 배나무의 배를 떨어뜨리기 위해 며느리의 방귀를 안전하게 사용할 수 있는 방법 찾기",
                  "examples": ["무엇을 사용할 것인지", "주변 사람들과 시아버지는 어디로 피해야 할지"]
                }
            """.trimIndent()

            val restored = converter.convertToEntityAttribute(dbData)

            restored shouldBe mission()
        }

        test("null이면 null을 반환한다") {
            converter.convertToEntityAttribute(null) shouldBe null
        }
    }

    context("왕복") {
        test("직렬화 후 복원하면 같은 값 객체가 된다") {
            val restored = converter.convertToEntityAttribute(converter.convertToDatabaseColumn(mission()))

            restored shouldBe mission()
        }

        test("빈 examples 목록도 왕복 보존된다") {
            val emptyExamples = Mission(goal = "목표", examples = emptyList())

            val restored = converter.convertToEntityAttribute(converter.convertToDatabaseColumn(emptyExamples))

            restored shouldBe emptyExamples
        }
    }
})
