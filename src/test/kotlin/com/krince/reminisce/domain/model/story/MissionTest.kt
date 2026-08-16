package com.krince.reminisce.domain.model.story

import com.krince.reminisce.domain.model.story.vo.MissionType
import com.krince.reminisce.domain.model.story.vo.WordCard
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@Tags("test", "unitTest")
@DisplayName("Mission 도메인 단위테스트")
class MissionTest : FunSpec({

    val wordCards = listOf(
        WordCard(text = "남들과", correctOrder = 1),
        WordCard(text = "달라도", correctOrder = 2),
        WordCard(text = "특별한 힘이", correctOrder = 3),
        WordCard(text = "될 수 있어요", correctOrder = 4),
    )

    context("WORD_ORDER 생성") {
        context("성공") {
            test("비어있지 않은 wordCards가 있으면 생성되고 값이 보존된다") {
                val mission = Mission(
                    goal = "문장 완성하기",
                    examples = listOf("남들과 다른 점을 좋은 힘으로 바꿔 보세요"),
                    type = MissionType.WORD_ORDER,
                    wordCards = wordCards,
                )

                mission.type shouldBe MissionType.WORD_ORDER
                mission.wordCards shouldBe wordCards
            }
        }
        context("실패") {
            test("wordCards가 null이면 생성할 수 없다") {
                shouldThrow<IllegalArgumentException> {
                    Mission(
                        goal = "문장 완성하기",
                        examples = listOf("예시"),
                        type = MissionType.WORD_ORDER,
                        wordCards = null,
                    )
                }
            }

            test("wordCards가 빈 목록이면 생성할 수 없다") {
                shouldThrow<IllegalArgumentException> {
                    Mission(
                        goal = "문장 완성하기",
                        examples = listOf("예시"),
                        type = MissionType.WORD_ORDER,
                        wordCards = emptyList(),
                    )
                }
            }
        }
    }

    context("SPEAKING 생성") {
        test("wordCards 없이 생성되고 기본 타입은 SPEAKING이다") {
            val mission = Mission(
                goal = "안전하게 배 떨어뜨리기",
                examples = listOf("무엇을 사용할지"),
            )

            mission.type shouldBe MissionType.SPEAKING
            mission.wordCards shouldBe null
        }
    }
})
