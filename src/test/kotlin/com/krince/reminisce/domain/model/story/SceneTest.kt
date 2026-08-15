package com.krince.reminisce.domain.model.story

import com.krince.reminisce.domain.model.story.vo.SceneId
import com.krince.reminisce.domain.model.story.vo.SceneType
import com.krince.reminisce.domain.model.story.vo.StoryId
import com.krince.reminisce.domain.model.story.vo.ThinkingElement
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe

@Tags("test", "unitTest")
@DisplayName("Scene 도메인 단위테스트")
class SceneTest : FunSpec({

    fun narrationScene(
        chapter: Int = 1,
        characterName: String? = null,
        characterDisplayName: String? = null,
        characterOpening: String? = null,
        characterClosing: String? = null,
        conflict: String? = null,
        sceneGoal: String? = null,
        requiredElements: List<ThinkingElement>? = null,
        preferredTurns: Int? = null,
        maxTurns: Int? = null,
    ): Scene = Scene(
        sceneId = SceneId("sc-narration-1"),
        storyId = StoryId("story-1"),
        sceneOrder = 1,
        chapter = chapter,
        sceneType = SceneType.NARRATION,
        sceneDescription = "전개 장면 설명",
        characterName = characterName,
        characterDisplayName = characterDisplayName,
        characterOpening = characterOpening,
        characterClosing = characterClosing,
        conflict = conflict,
        sceneGoal = sceneGoal,
        requiredElements = requiredElements,
        preferredTurns = preferredTurns,
        maxTurns = maxTurns,
    )

    fun dialogueScene(
        chapter: Int = 1,
        characterName: String? = "ch_banggui_daughter_in_law",
        characterDisplayName: String? = "방귀쟁이 며느리",
        characterOpening: String? = "고정 첫 대사",
        characterClosing: String? = "고정 마지막 대사",
        conflict: String? = null,
        sceneGoal: String? = "장면 발화 목표",
        requiredElements: List<ThinkingElement>? = listOf(ThinkingElement.PERSPECTIVE, ThinkingElement.EMOTION),
        preferredTurns: Int? = null,
        maxTurns: Int? = 4,
        characterImageUrl: String? = null,
    ): Scene = Scene(
        sceneId = SceneId("sc-dialogue-1"),
        storyId = StoryId("story-1"),
        sceneOrder = 3,
        chapter = chapter,
        sceneType = SceneType.DIALOGUE,
        sceneDescription = "대화 장면 설명",
        characterName = characterName,
        characterDisplayName = characterDisplayName,
        characterOpening = characterOpening,
        characterClosing = characterClosing,
        conflict = conflict,
        sceneGoal = sceneGoal,
        requiredElements = requiredElements,
        preferredTurns = preferredTurns,
        maxTurns = maxTurns,
        characterImageUrl = characterImageUrl,
    )

    val characterVoice = CharacterVoice(
        gender = VoiceGender.FEMALE,
        ageGroup = VoiceAgeGroup.ADULT,
        voiceProfile = "young_woman_gentle",
    )

    fun characterLineScene(
        characterName: String? = "ch_banggui_daughter_in_law",
        characterDisplayName: String? = "방귀쟁이 며느리",
        characterOpening: String? = "ㅇㅇ아, 안녕?",
        characterVoiceValue: CharacterVoice? = characterVoice,
        sceneGoal: String? = null,
        requiredElements: List<ThinkingElement>? = null,
        maxTurns: Int? = null,
        mission: Mission? = null,
        characterImageUrl: String? = null,
    ): Scene = Scene(
        sceneId = SceneId("sc-character-line-1"),
        storyId = StoryId("story-1"),
        sceneOrder = 2,
        sceneType = SceneType.CHARACTER_LINE,
        sceneDescription = "캐릭터 대사 장면 설명",
        characterName = characterName,
        characterDisplayName = characterDisplayName,
        characterOpening = characterOpening,
        sceneGoal = sceneGoal,
        requiredElements = requiredElements,
        maxTurns = maxTurns,
        mission = mission,
        characterVoice = characterVoiceValue,
        characterImageUrl = characterImageUrl,
    )

    context("NARRATION 생성") {
        context("성공") {
            test("대화 전용 필드가 전부 null이면 생성된다") {
                val scene = narrationScene()

                scene.sceneType shouldBe SceneType.NARRATION
                scene.sceneDescription shouldBe "전개 장면 설명"
                scene.characterName shouldBe null
                scene.requiredElements shouldBe null
                scene.maxTurns shouldBe null
                scene.characterImageUrl shouldBe null
            }
        }
        context("실패") {
            test("대화 전용 필드가 하나라도 있으면 생성할 수 없다") {
                val invalidCreations: List<() -> Scene> = listOf(
                    { narrationScene(characterName = "ch_banggui_daughter_in_law") },
                    { narrationScene(characterDisplayName = "방귀쟁이 며느리") },
                    { narrationScene(characterOpening = "고정 첫 대사") },
                    { narrationScene(characterClosing = "고정 마지막 대사") },
                    { narrationScene(conflict = "갈등 요약") },
                    { narrationScene(sceneGoal = "장면 발화 목표") },
                    { narrationScene(requiredElements = listOf(ThinkingElement.REASON)) },
                    { narrationScene(preferredTurns = 3) },
                    { narrationScene(maxTurns = 4) },
                )

                invalidCreations.forEach { creation ->
                    shouldThrow<IllegalArgumentException> { creation() }
                }
            }
        }
    }

    context("DIALOGUE 생성") {
        context("성공") {
            test("필수 대화 필드가 모두 있으면 생성되고 값이 보존된다") {
                val scene = dialogueScene()

                scene.sceneType shouldBe SceneType.DIALOGUE
                scene.characterName shouldBe "ch_banggui_daughter_in_law"
                scene.characterDisplayName shouldBe "방귀쟁이 며느리"
                scene.characterOpening shouldBe "고정 첫 대사"
                scene.characterClosing shouldBe "고정 마지막 대사"
                scene.sceneGoal shouldBe "장면 발화 목표"
                scene.requiredElements shouldContainExactly listOf(
                    ThinkingElement.PERSPECTIVE,
                    ThinkingElement.EMOTION,
                )
                scene.maxTurns shouldBe 4
            }

            test("conflict와 preferredTurns는 저작 값이 없어도 생성된다") {
                val scene = dialogueScene(conflict = null, preferredTurns = null)

                scene.conflict shouldBe null
                scene.preferredTurns shouldBe null
            }

            test("conflict와 preferredTurns가 있으면 값이 보존된다") {
                val scene = dialogueScene(conflict = "갈등 요약", preferredTurns = 3)

                scene.conflict shouldBe "갈등 요약"
                scene.preferredTurns shouldBe 3
            }

            test("characterImageUrl 기본값은 null이고 값을 주면 보존된다") {
                dialogueScene().characterImageUrl shouldBe null
                dialogueScene(
                    characterImageUrl = "/files/char-ch_banggui_daughter_in_law.png",
                ).characterImageUrl shouldBe "/files/char-ch_banggui_daughter_in_law.png"
            }

            test("개인화 복사본도 characterImageUrl을 그대로 전달한다") {
                val personalized = dialogueScene(
                    characterOpening = "ㅇㅇ아, 안녕?",
                    characterImageUrl = "/files/char-ch_banggui_daughter_in_law.png",
                ).personalizedFor("지우")

                personalized.characterImageUrl shouldBe "/files/char-ch_banggui_daughter_in_law.png"
            }

            test("chapter 값이 보존되고 개인화 복사본도 chapter를 그대로 전달한다") {
                val personalized = dialogueScene(
                    chapter = 2,
                    characterOpening = "ㅇㅇ아, 안녕?",
                ).personalizedFor("지우")

                dialogueScene(chapter = 2).chapter shouldBe 2
                personalized.chapter shouldBe 2
            }
        }
        context("실패") {
            test("필수 대화 필드가 하나라도 없으면 생성할 수 없다") {
                val invalidCreations: List<() -> Scene> = listOf(
                    { dialogueScene(characterName = null) },
                    { dialogueScene(characterDisplayName = null) },
                    { dialogueScene(characterOpening = null) },
                    { dialogueScene(characterClosing = null) },
                    { dialogueScene(sceneGoal = null) },
                    { dialogueScene(requiredElements = null) },
                    { dialogueScene(maxTurns = null) },
                )

                invalidCreations.forEach { creation ->
                    shouldThrow<IllegalArgumentException> { creation() }
                }
            }

            test("requiredElements가 빈 목록이면 생성할 수 없다") {
                shouldThrow<IllegalArgumentException> { dialogueScene(requiredElements = emptyList()) }
            }
        }
    }

    context("CHARACTER_LINE 생성") {
        context("성공") {
            test("필수 캐릭터 대사 필드가 모두 있으면 생성되고 값이 보존된다") {
                val scene = characterLineScene(characterImageUrl = "/files/char-ch_banggui_daughter_in_law.png")

                scene.sceneType shouldBe SceneType.CHARACTER_LINE
                scene.characterName shouldBe "ch_banggui_daughter_in_law"
                scene.characterDisplayName shouldBe "방귀쟁이 며느리"
                scene.characterOpening shouldBe "ㅇㅇ아, 안녕?"
                scene.characterVoice shouldBe characterVoice
                scene.characterImageUrl shouldBe "/files/char-ch_banggui_daughter_in_law.png"
                scene.sceneGoal shouldBe null
                scene.requiredElements shouldBe null
                scene.maxTurns shouldBe null
                scene.mission shouldBe null
                scene.characterClosing shouldBe null
            }

            test("characterImageUrl은 없어도 생성된다") {
                characterLineScene().characterImageUrl shouldBe null
            }

            test("개인화 복사본은 characterOpening 애칭을 치환하고 characterVoice를 그대로 전달한다") {
                val personalized = characterLineScene().personalizedFor("지우")

                personalized.characterOpening shouldBe "지우야, 안녕?"
                personalized.characterVoice shouldBe characterVoice
            }
        }
        context("실패") {
            test("필수 캐릭터 대사 필드가 하나라도 없으면 생성할 수 없다") {
                val invalidCreations: List<() -> Scene> = listOf(
                    { characterLineScene(characterName = null) },
                    { characterLineScene(characterDisplayName = null) },
                    { characterLineScene(characterOpening = null) },
                    { characterLineScene(characterVoiceValue = null) },
                )

                invalidCreations.forEach { creation ->
                    shouldThrow<IllegalArgumentException> { creation() }
                }
            }

            test("인터랙티브 전용 필드가 하나라도 있으면 생성할 수 없다") {
                val invalidCreations: List<() -> Scene> = listOf(
                    { characterLineScene(sceneGoal = "장면 발화 목표") },
                    { characterLineScene(requiredElements = listOf(ThinkingElement.PERSPECTIVE)) },
                    { characterLineScene(maxTurns = 4) },
                    { characterLineScene(mission = Mission(goal = "목표", examples = listOf("예시"))) },
                )

                invalidCreations.forEach { creation ->
                    shouldThrow<IllegalArgumentException> { creation() }
                }
            }
        }
    }
})
