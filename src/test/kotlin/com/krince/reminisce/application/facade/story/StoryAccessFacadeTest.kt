package com.krince.reminisce.application.facade.story

import com.krince.reminisce.application.port.out.story.LoadStoryPort
import com.krince.reminisce.domain.model.story.CharacterVoice
import com.krince.reminisce.domain.model.story.Scene
import com.krince.reminisce.domain.model.story.Story
import com.krince.reminisce.domain.model.story.VoiceAgeGroup
import com.krince.reminisce.domain.model.story.VoiceGender
import com.krince.reminisce.domain.model.story.vo.Difficulty
import com.krince.reminisce.domain.model.story.vo.SceneId
import com.krince.reminisce.domain.model.story.vo.SceneType
import com.krince.reminisce.domain.model.story.vo.StoryId
import com.krince.reminisce.domain.model.story.vo.StoryStatus
import com.krince.reminisce.domain.model.story.vo.ThinkingElement
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk

@Tags("test", "unitTest")
@DisplayName("StoryAccessFacade 단위테스트")
class StoryAccessFacadeTest : FunSpec({

    val loadStoryPort = mockk<LoadStoryPort>()
    val facade = StoryAccessFacade(loadStoryPort)

    beforeEach { clearAllMocks() }

    val storyId = StoryId("story-uuid-1")
    val daughterName = "ch_banggui_daughter_in_law"
    val fatherName = "ch_banggui_father_in_law"
    val characterVoice = CharacterVoice(
        gender = VoiceGender.FEMALE,
        ageGroup = VoiceAgeGroup.ADULT,
        voiceProfile = "young_woman_gentle",
    )

    fun narrationScene(sceneId: String, sceneOrder: Int, chapter: Int = 1): Scene = Scene(
        sceneId = SceneId(sceneId),
        storyId = storyId,
        sceneOrder = sceneOrder,
        chapter = chapter,
        sceneType = SceneType.NARRATION,
        sceneDescription = "전개 설명 $sceneOrder",
    )

    fun characterLineScene(
        sceneId: String,
        sceneOrder: Int,
        characterName: String = daughterName,
        characterLine: String = "캐릭터 대사 $sceneOrder",
        chapter: Int = 1,
    ): Scene = Scene(
        sceneId = SceneId(sceneId),
        storyId = storyId,
        sceneOrder = sceneOrder,
        chapter = chapter,
        sceneType = SceneType.CHARACTER_LINE,
        sceneDescription = "캐릭터 대사 장면 설명 $sceneOrder",
        characterName = characterName,
        characterDisplayName = "표시명-$characterName",
        characterOpening = characterLine,
        characterVoice = characterVoice,
    )

    fun dialogueScene(
        sceneId: String,
        sceneOrder: Int,
        characterName: String = daughterName,
        chapter: Int = 1,
    ): Scene = Scene(
        sceneId = SceneId(sceneId),
        storyId = storyId,
        sceneOrder = sceneOrder,
        chapter = chapter,
        sceneType = SceneType.DIALOGUE,
        sceneDescription = "대화 장면 설명 $sceneOrder",
        characterName = characterName,
        characterDisplayName = "표시명-$characterName",
        sceneGoal = "장면 발화 목표",
        requiredElements = listOf(ThinkingElement.PERSPECTIVE, ThinkingElement.EMOTION),
        maxTurns = 4,
        characterVoice = characterVoice,
        characterImageUrl = "/files/char-$characterName.png",
    )

    fun story(
        scenes: List<Scene>,
        difficulty: Difficulty = Difficulty("보통"),
        representativeImageUrl: String? = null,
        topics: List<String> = emptyList(),
    ): Story = Story(
        storyId = storyId,
        title = "방귀 뀌는 며느리",
        summary = "이야기 요약",
        intro = "이야기 도입",
        situation = null,
        childRole = null,
        difficulty = difficulty,
        estimatedMinutes = 20,
        representativeImageUrl = representativeImageUrl,
        status = StoryStatus.PUBLISHED,
        postActivityConfig = null,
        topics = topics,
        scenes = scenes,
    )

    context("findPrecedingCharacterLine") {
        context("성공") {
            test("같은 캐릭터의 직전 CHARACTER_LINE 중 가장 가까운 신을 반환한다") {
                every { loadStoryPort.findByIdWithScenesPublished(storyId) } returns story(
                    scenes = listOf(
                        dialogueScene("sc-4", 4),
                        characterLineScene("sc-2", 2, characterLine = "먼 오프닝"),
                        characterLineScene("sc-3", 3, characterLine = "가까운 오프닝"),
                        narrationScene("sc-1", 1),
                    ),
                )

                val result = facade.findPrecedingCharacterLine(storyId, "sc-4")

                result?.sceneId?.value shouldBe "sc-3"
                result?.characterOpening shouldBe "가까운 오프닝"
            }

            test("다른 캐릭터의 CHARACTER_LINE은 건너뛰고 같은 캐릭터의 신을 반환한다") {
                every { loadStoryPort.findByIdWithScenesPublished(storyId) } returns story(
                    scenes = listOf(
                        narrationScene("sc-1", 1),
                        characterLineScene("sc-2", 2, characterName = daughterName, characterLine = "며느리 오프닝"),
                        characterLineScene("sc-3", 3, characterName = fatherName, characterLine = "시아버지 오프닝"),
                        dialogueScene("sc-4", 4, characterName = daughterName),
                    ),
                )

                val result = facade.findPrecedingCharacterLine(storyId, "sc-4")

                result?.sceneId?.value shouldBe "sc-2"
                result?.characterName shouldBe daughterName
            }
        }

        context("부재") {
            test("직전에 같은 캐릭터의 CHARACTER_LINE이 없으면 null을 반환한다") {
                every { loadStoryPort.findByIdWithScenesPublished(storyId) } returns story(
                    scenes = listOf(
                        narrationScene("sc-1", 1),
                        dialogueScene("sc-2", 2),
                    ),
                )

                facade.findPrecedingCharacterLine(storyId, "sc-2") shouldBe null
            }

            test("현재 신보다 뒤에 있는 CHARACTER_LINE은 고려하지 않는다") {
                every { loadStoryPort.findByIdWithScenesPublished(storyId) } returns story(
                    scenes = listOf(
                        dialogueScene("sc-1", 1),
                        characterLineScene("sc-2", 2),
                    ),
                )

                facade.findPrecedingCharacterLine(storyId, "sc-1") shouldBe null
            }

            test("공개 이야기가 없으면 null을 반환한다") {
                every { loadStoryPort.findByIdWithScenesPublished(storyId) } returns null

                facade.findPrecedingCharacterLine(storyId, "sc-1") shouldBe null
            }

            test("현재 신을 찾지 못하면 null을 반환한다") {
                every { loadStoryPort.findByIdWithScenesPublished(storyId) } returns story(
                    scenes = listOf(characterLineScene("sc-1", 1)),
                )

                facade.findPrecedingCharacterLine(storyId, "unknown-scene") shouldBe null
            }
        }
    }

    context("findResumableDisplayInfo") {
        test("currentSceneId가 2번째 챕터 신이면 currentChapter=2·totalChapters=최대 챕터로 계산한다") {
            every { loadStoryPort.findByIdWithScenesPublished(storyId) } returns story(
                scenes = listOf(
                    narrationScene("sc-1", 1, chapter = 1),
                    narrationScene("sc-2", 2, chapter = 2),
                    dialogueScene("sc-3", 3, chapter = 3),
                ),
            )

            val result = facade.findResumableDisplayInfo(storyId, "sc-2")

            result?.currentChapter shouldBe 2
            result?.totalChapters shouldBe 3
        }

        test("currentSceneId가 null이면 currentChapter를 기본값 0으로 채운다") {
            every { loadStoryPort.findByIdWithScenesPublished(storyId) } returns story(
                scenes = listOf(
                    narrationScene("sc-1", 1, chapter = 1),
                    narrationScene("sc-2", 2, chapter = 2),
                ),
            )

            val result = facade.findResumableDisplayInfo(storyId, null)

            result?.currentChapter shouldBe 0
            result?.totalChapters shouldBe 2
        }

        test("title·representativeImageUrl·difficulty·topics를 Story 값 그대로 매핑한다") {
            every { loadStoryPort.findByIdWithScenesPublished(storyId) } returns story(
                scenes = listOf(narrationScene("sc-1", 1, chapter = 1)),
                difficulty = Difficulty("어려움"),
                representativeImageUrl = "/files/cover.png",
                topics = listOf("공감", "존중"),
            )

            val result = facade.findResumableDisplayInfo(storyId, "sc-1")

            result?.title shouldBe "방귀 뀌는 며느리"
            result?.representativeImageUrl shouldBe "/files/cover.png"
            result?.difficulty shouldBe "어려움"
            result?.topics shouldBe listOf("공감", "존중")
        }

        test("공개 이야기가 없으면 null을 반환한다") {
            every { loadStoryPort.findByIdWithScenesPublished(storyId) } returns null

            facade.findResumableDisplayInfo(storyId, "sc-1") shouldBe null
        }
    }
})
