package com.krince.reminisce.domain.model.story

import com.krince.reminisce.domain.model.story.vo.Difficulty
import com.krince.reminisce.domain.model.story.vo.SceneId
import com.krince.reminisce.domain.model.story.vo.SceneType
import com.krince.reminisce.domain.model.story.vo.StoryId
import com.krince.reminisce.domain.model.story.vo.StoryStatus
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe

@Tags("test", "unitTest")
@DisplayName("Story 도메인 단위테스트")
class StoryTest : FunSpec({

    fun narrationScene(sceneId: String, sceneOrder: Int): Scene = Scene(
        sceneId = SceneId(sceneId),
        storyId = StoryId("story-1"),
        sceneOrder = sceneOrder,
        sceneType = SceneType.NARRATION,
        sceneDescription = "전개 장면 설명 $sceneOrder",
    )

    fun story(scenes: List<Scene>): Story = Story(
        storyId = StoryId("story-1"),
        title = "방귀 뀌는 며느리",
        summary = "큰 방귀를 부끄러워하던 며느리가 자신의 다름을 장점으로 바꾸는 이야기",
        intro = "이야기 도입",
        situation = null,
        childRole = null,
        difficulty = Difficulty("보통"),
        estimatedMinutes = 20,
        representativeImageUrl = null,
        status = StoryStatus.PUBLISHED,
        postActivityConfig = null,
        topics = listOf("다름", "자기이해"),
        scenes = scenes,
    )

    context("생성") {
        context("성공") {
            test("뒤섞인 장면 목록을 넣어도 sceneOrder 오름차순으로 보관한다") {
                val result = story(
                    scenes = listOf(
                        narrationScene("sc-3", 3),
                        narrationScene("sc-1", 1),
                        narrationScene("sc-2", 2),
                    ),
                )

                result.scenes.map { it.sceneOrder } shouldContainExactly listOf(1, 2, 3)
                result.scenes.map { it.sceneId.value } shouldContainExactly listOf("sc-1", "sc-2", "sc-3")
            }

            test("장면이 없는 목록 조회용 이야기도 생성된다") {
                val result = story(scenes = emptyList())

                result.scenes.shouldBeEmpty()
            }

            test("이야기 필드 값이 보존된다") {
                val result = story(scenes = emptyList())

                result.storyId.value shouldBe "story-1"
                result.title shouldBe "방귀 뀌는 며느리"
                result.difficulty.value shouldBe "보통"
                result.estimatedMinutes shouldBe 20
                result.status shouldBe StoryStatus.PUBLISHED
                result.topics shouldContainExactly listOf("다름", "자기이해")
            }
        }
        context("실패") {
            test("sceneOrder가 중복되면 생성할 수 없다") {
                shouldThrow<IllegalArgumentException> {
                    story(
                        scenes = listOf(
                            narrationScene("sc-1", 1),
                            narrationScene("sc-duplicate", 1),
                        ),
                    )
                }
            }
        }
    }
})
