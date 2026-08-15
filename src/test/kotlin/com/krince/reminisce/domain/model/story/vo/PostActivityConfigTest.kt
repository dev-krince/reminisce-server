package com.krince.reminisce.domain.model.story.vo

import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe

@Tags("test", "unitTest")
@DisplayName("PostActivityConfig VO 단위테스트")
class PostActivityConfigTest : FunSpec({

    context("생성") {
        context("성공") {
            test("카드와 재구성 핵심 단어를 그대로 보존한다") {
                val config = PostActivityConfig(
                    cards = listOf(
                        PostActivityConfig.Card(id = "card_1", text = "용왕이 병에 걸렸어요.", correctOrder = 1),
                        PostActivityConfig.Card(id = "card_2", text = "자라가 토끼를 찾아갔어요.", correctOrder = 2),
                    ),
                    retellingKeywords = listOf("용왕", "자라", "토끼", "용궁"),
                )

                config shouldBe PostActivityConfig(
                    cards = listOf(
                        PostActivityConfig.Card(id = "card_1", text = "용왕이 병에 걸렸어요.", correctOrder = 1),
                        PostActivityConfig.Card(id = "card_2", text = "자라가 토끼를 찾아갔어요.", correctOrder = 2),
                    ),
                    retellingKeywords = listOf("용왕", "자라", "토끼", "용궁"),
                )
            }

            test("빈 카드 목록과 빈 핵심 단어 목록도 허용된다") {
                val config = PostActivityConfig(cards = emptyList(), retellingKeywords = emptyList())

                config.cards.shouldBeEmpty()
                config.retellingKeywords.shouldBeEmpty()
            }

            test("카드 imageUrl 기본값은 null이고 값을 주면 보존된다") {
                PostActivityConfig.Card(id = "card_1", text = "카드", correctOrder = 1).imageUrl shouldBe null
                PostActivityConfig.Card(
                    id = "card_1",
                    text = "카드",
                    correctOrder = 1,
                    imageUrl = "/files/banggui-card-1.png",
                ).imageUrl shouldBe "/files/banggui-card-1.png"
            }
        }
    }
})
