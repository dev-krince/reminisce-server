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

    fun configWithCardImages(): PostActivityConfig = PostActivityConfig(
        cards = listOf(
            PostActivityConfig.Card(id = "card_1", text = "용왕이 병에 걸렸어요.", correctOrder = 1, imageUrl = "/files/banggui-card-1.png"),
            PostActivityConfig.Card(id = "card_2", text = "자라가 토끼를 찾아갔어요.", correctOrder = 2, imageUrl = "/files/banggui-card-2.png"),
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

        test("카드 imageUrl이 있으면 image_url 키로 직렬화하고 없으면 키를 넣지 않는다") {
            val dbData = converter.convertToDatabaseColumn(
                PostActivityConfig(
                    cards = listOf(
                        PostActivityConfig.Card(id = "card_1", text = "카드1", correctOrder = 1, imageUrl = "/files/banggui-card-1.png"),
                        PostActivityConfig.Card(id = "card_2", text = "카드2", correctOrder = 2),
                    ),
                    retellingKeywords = emptyList(),
                ),
            )

            dbData.shouldNotBeNull()
            val rootNode = objectMapper.readTree(dbData)
            rootNode.path("cards")[0].path("image_url").asText() shouldBe "/files/banggui-card-1.png"
            rootNode.path("cards")[1].has("image_url") shouldBe false
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

        test("image_url 키가 없는 레거시 카드 JSON은 imageUrl=null로 복원한다") {
            val legacyDbData = """
                {
                  "cards": [
                    { "id": "card_1", "text": "용왕이 병에 걸렸어요.", "correct_order": 1 }
                  ],
                  "retelling_keywords": ["용왕"]
                }
            """.trimIndent()

            val restored = converter.convertToEntityAttribute(legacyDbData)

            restored shouldBe PostActivityConfig(
                cards = listOf(PostActivityConfig.Card(id = "card_1", text = "용왕이 병에 걸렸어요.", correctOrder = 1)),
                retellingKeywords = listOf("용왕"),
            )
        }

        test("image_url 키가 있는 카드 JSON은 imageUrl로 복원한다") {
            val dbData = """
                {
                  "cards": [
                    { "id": "card_1", "text": "용왕이 병에 걸렸어요.", "correct_order": 1, "image_url": "/files/banggui-card-1.png" }
                  ],
                  "retelling_keywords": ["용왕"]
                }
            """.trimIndent()

            val restored = converter.convertToEntityAttribute(dbData)

            restored shouldBe PostActivityConfig(
                cards = listOf(
                    PostActivityConfig.Card(id = "card_1", text = "용왕이 병에 걸렸어요.", correctOrder = 1, imageUrl = "/files/banggui-card-1.png"),
                ),
                retellingKeywords = listOf("용왕"),
            )
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

        test("카드 imageUrl도 직렬화 후 복원하면 같은 값 객체가 된다") {
            val restored = converter.convertToEntityAttribute(converter.convertToDatabaseColumn(configWithCardImages()))

            restored shouldBe configWithCardImages()
        }
    }
})
