package com.krince.reminisce.infra.seed

import com.krince.reminisce.domain.model.story.vo.MissionType
import com.krince.reminisce.infra.adapter.out.persistence.story.SceneRepository
import com.krince.reminisce.infra.adapter.out.persistence.story.StoryRepository
import com.krince.reminisce.infra.adapter.out.persistence.story.StoryTopicRepository
import com.krince.reminisce.infra.adapter.out.persistence.story.entity.SceneOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.story.entity.StoryOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.story.entity.StoryTopicOrmEntity
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.springframework.boot.ApplicationArguments
import java.util.Optional

@Tags("test", "unitTest")
@DisplayName("StoryContentSeeder 단위테스트")
class StoryContentSeederTest : FunSpec({

    val storyRepository = mockk<StoryRepository>()
    val sceneRepository = mockk<SceneRepository>()
    val storyTopicRepository = mockk<StoryTopicRepository>()
    val seeder = StoryContentSeeder(storyRepository, sceneRepository, storyTopicRepository)
    val applicationArguments = mockk<ApplicationArguments>()

    val bangguiStoryId = "s_banggui_daughter_in_law_001"
    val narrationType = "NARRATION"
    val characterLineType = "CHARACTER_LINE"
    val dialogueType = "DIALOGUE"

    fun existingStoryEntity(storyGenre: String?): StoryOrmEntity = StoryOrmEntity(
        storyId = bangguiStoryId,
        title = "방귀 뀌는 며느리",
        summary = "기존 요약",
        intro = "기존 도입",
        situation = null,
        childRole = null,
        difficulty = "보통",
        estimatedMinutes = 20,
        representativeImageUrl = null,
        status = "PUBLISHED",
        storyGenre = storyGenre,
        postActivityConfig = null,
    )

    fun seedScenes(): List<SceneOrmEntity> {
        every { storyRepository.findById(bangguiStoryId) } returns Optional.empty()
        every { storyRepository.save(any()) } answers { firstArg() }
        val sceneSlot = slot<List<SceneOrmEntity>>()
        every { sceneRepository.saveAll(capture(sceneSlot)) } answers { sceneSlot.captured }
        every { storyTopicRepository.saveAll(any<List<StoryTopicOrmEntity>>()) } answers { firstArg() }

        seeder.run(applicationArguments)

        return sceneSlot.captured
    }

    beforeEach { clearAllMocks() }

    context("시딩") {
        test("스토리가 없으면 17신을 sceneOrder 오름차순·챕터 비감소로 저장하고 챕터마다 CHARACTER_LINE이 2개 이상이다") {
            val scenes = seedScenes()

            scenes shouldHaveSize 17
            scenes.map { it.sceneOrder.toInt() } shouldContainExactly (1..17).toList()
            scenes.map { it.chapter.toInt() }.zipWithNext().all { (previous, next) -> previous <= next }.shouldBeTrue()
            scenes.map { it.chapter.toInt() }.distinct() shouldContainExactly listOf(1, 2, 3, 4)
            (1..4).forEach { chapter ->
                scenes.count { it.chapter.toInt() == chapter && it.sceneType == characterLineType } shouldBe 2
            }
            scenes.filter { it.chapter.toInt() == 1 }.map { it.sceneType } shouldContainExactly listOf(
                narrationType, narrationType, characterLineType, dialogueType, characterLineType,
            )
            (2..4).forEach { chapter ->
                scenes.filter { it.chapter.toInt() == chapter }.map { it.sceneType } shouldContainExactly listOf(
                    narrationType, characterLineType, dialogueType, characterLineType,
                )
            }
            scenes.all { it.imageUrl != null }.shouldBeTrue()
        }

        test("DIALOGUE 신은 고정 대사 없이 목표·요소·턴·음성·아바타만 갖는 순수 인터랙티브 슬롯이고 정본 값(캐릭터·maxTurns 4/5/5/4)을 유지한다") {
            val dialogues = seedScenes().filter { it.sceneType == dialogueType }

            dialogues shouldHaveSize 4
            dialogues.forEach { dialogue ->
                dialogue.characterOpening shouldBe null
                dialogue.characterClosing shouldBe null
                dialogue.sceneGoal.shouldNotBeNull()
                dialogue.requiredElements.shouldNotBeNull() shouldHaveSize 4
                dialogue.maxTurns.shouldNotBeNull()
                dialogue.characterVoice.shouldNotBeNull()
                dialogue.characterImageUrl.shouldNotBeNull()
            }
            dialogues.map { it.characterName } shouldContainExactly listOf(
                "ch_banggui_daughter_in_law",
                "ch_banggui_father_in_law",
                "ch_banggui_village_chief",
                "ch_banggui_daughter_in_law",
            )
            dialogues.map { it.maxTurns?.toInt() } shouldContainExactly listOf(4, 5, 5, 4)
        }

        test("CHARACTER_LINE 신은 확정 스크립트 대사를 자구 그대로 담고 인터랙티브 필드를 갖지 않는다") {
            val characterLines = seedScenes().filter { it.sceneType == characterLineType }

            characterLines shouldHaveSize 8
            characterLines.forEach { characterLine ->
                characterLine.characterVoice.shouldNotBeNull()
                characterLine.characterImageUrl.shouldNotBeNull()
                characterLine.sceneGoal shouldBe null
                characterLine.requiredElements shouldBe null
                characterLine.maxTurns shouldBe null
                characterLine.mission shouldBe null
            }
            characterLines.map { it.characterOpening } shouldContainExactly listOf(
                "ㅇㅇ아, 내 방귀가 너무 크다는 걸 알면 가족들이 나를 이상하게 생각하지 않을까?",
                "그래도 아직은 못 말하겠어. 조금만 더 참아 볼게.",
                "아이고, 이게 무슨 일이냐! 우리 집안이 다 흔들리는구나! 이렇게 창피한 며느리와 함께 못 살겠다! ㅇㅇ도 그렇게 생각하지 않니?",
                "흥, 그래도 도저히 이런 며느리와는 함께 살 수 없으니 친정으로 데려다줘야겠다.",
                "이 배나무는 해마다 탐스러운 배가 열리지만, 너무 높아서 아무도 딸 수가 없단다. 무슨 뾰족한 방법이 없겠는가?",
                "아이고, 방귀 뀌는 며느리 덕분에 온 마을이 배 잔치를 할 수 있겠구려, 고맙소!",
                "ㅇㅇ이 덕분에 내 방귀가 누군가에게 도움이 될 수 있다는 걸 처음 알았어. 이제는 방귀 소리가 큰 걸 부끄러워하지 않아도 될까?",
                "이제는 부끄러워하며 숨기지 않고, 조심해서 좋은 일에 써 볼게.",
            )
        }

        test("미션은 3·4챕터 DIALOGUE에만 부여된다") {
            val scenes = seedScenes()

            scenes.filter { it.mission != null }.map { it.chapter.toInt() } shouldContainExactly listOf(3, 4)
            scenes.filter { it.mission != null }.all { it.sceneType == dialogueType }.shouldBeTrue()
        }

        test("sc_banggui_12는 SPEAKING 미션이고 sc_banggui_16은 4개 단어카드를 정답순서로 갖는 WORD_ORDER 미션이다") {
            val scenes = seedScenes()

            val pearDrop = scenes.first { it.sceneId == "sc_banggui_12" }.mission.shouldNotBeNull()
            pearDrop.type shouldBe MissionType.SPEAKING
            pearDrop.wordCards shouldBe null

            val reframe = scenes.first { it.sceneId == "sc_banggui_16" }.mission.shouldNotBeNull()
            reframe.type shouldBe MissionType.WORD_ORDER
            reframe.wordCards.shouldNotBeNull() shouldHaveSize 4
            reframe.wordCards.sortedBy { it.correctOrder }.map { it.text } shouldContainExactly listOf(
                "남들과", "달라도", "특별한 힘이", "될 수 있어요",
            )
        }
    }

    context("스킵") {
        test("장르가 채워진 스토리가 이미 있으면 아무것도 저장하지 않는다") {
            every { storyRepository.findById(bangguiStoryId) } returns
                Optional.of(existingStoryEntity(storyGenre = "FOLKTALE"))

            seeder.run(applicationArguments)

            verify(exactly = 0) { storyRepository.save(any()) }
            verify(exactly = 0) { sceneRepository.saveAll(any<List<SceneOrmEntity>>()) }
            verify(exactly = 0) { storyTopicRepository.saveAll(any<List<StoryTopicOrmEntity>>()) }
        }

        test("장르가 빈 스토리가 이미 있으면 장르만 채우고 신은 저장하지 않는다") {
            every { storyRepository.findById(bangguiStoryId) } returns
                Optional.of(existingStoryEntity(storyGenre = null))
            val backfilledSlot = slot<StoryOrmEntity>()
            every { storyRepository.save(capture(backfilledSlot)) } answers { backfilledSlot.captured }

            seeder.run(applicationArguments)

            backfilledSlot.captured.storyGenre shouldBe "FOLKTALE"
            verify(exactly = 1) { storyRepository.save(any()) }
            verify(exactly = 0) { sceneRepository.saveAll(any<List<SceneOrmEntity>>()) }
        }
    }
})
