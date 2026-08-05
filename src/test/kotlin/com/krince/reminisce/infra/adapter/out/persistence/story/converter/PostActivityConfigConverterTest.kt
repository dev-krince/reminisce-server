package com.krince.reminisce.infra.adapter.out.persistence.story.converter

import com.fasterxml.jackson.databind.ObjectMapper
import com.krince.reminisce.domain.model.story.vo.PostActivityConfig
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

@Tags("test", "unitTest")
@DisplayName("PostActivityConfigConverter 단위테스트")
class PostActivityConfigConverterTest : FunSpec({

    val converter = PostActivityConfigConverter()
    val objectMapper = ObjectMapper()

    fun config(): PostActivityConfig = PostActivityConfig(
        cards = listOf(
            PostActivityConfig.Card(id = "card_1", text = "용왕이 병에 걸렸어요.", correctOrder = 1),
            PostActivityConfig.Card(id = "card_2", text = "자라가 토끼를 찾아갔어요.", correctOrder = 2),
        ),
        retellingKeywords = listOf("용왕", "자라", "토끼", "용궁"),
    )

    context("convertToDatabaseColumn") {
        test("DB 정본 JSON 키(cards·id·text·correct_order·retelling_keywords)로 직렬화한다") {
            val dbData = converter.convertToDatabaseColumn(config())

            dbData.shouldNotBeNull()
            val rootNode = objectMapper.readTree(dbData)
            rootNode.path("cards").size() shouldBe 2
            rootNode.path("cards")[0].path("id").asText() shouldBe "card_1"
            rootNode.path("cards")[0].path("text").asText() shouldBe "용왕이 병에 걸렸어요."
            rootNode.path("cards")[0].path("correct_order").asInt() shouldBe 1
            rootNode.path("cards")[1].path("correct_order").asInt() shouldBe 2
            rootNode.path("retelling_keywords").map { it.asText() } shouldBe listOf("용왕", "자라", "토끼", "용궁")
        }

        test("null이면 null을 반환한다") {
            converter.convertToDatabaseColumn(null) shouldBe null
        }
    }

    context("convertToEntityAttribute") {
        test("DB 정본 예시 JSON을 도메인 값 객체로 복원한다") {
            val dbData = """
                {
                  "cards": [
                    { "id": "card_1", "text": "용왕이 병에 걸렸어요.", "correct_order": 1 },
                    { "id": "card_2", "text": "자라가 토끼를 찾아갔어요.", "correct_order": 2 }
                  ],
                  "retelling_keywords": ["용왕", "자라", "토끼", "용궁"]
                }
            """.trimIndent()

            val restored = converter.convertToEntityAttribute(dbData)

            restored shouldBe config()
        }

        test("null이면 null을 반환한다") {
            converter.convertToEntityAttribute(null) shouldBe null
        }
    }

    context("왕복") {
        test("직렬화 후 복원하면 같은 값 객체가 된다") {
            val restored = converter.convertToEntityAttribute(converter.convertToDatabaseColumn(config()))

            restored shouldBe config()
        }

        test("빈 카드 목록과 빈 핵심 단어 목록도 왕복 보존된다") {
            val emptyConfig = PostActivityConfig(cards = emptyList(), retellingKeywords = emptyList())

            val restored = converter.convertToEntityAttribute(converter.convertToDatabaseColumn(emptyConfig))

            restored shouldBe emptyConfig
        }
    }
})
