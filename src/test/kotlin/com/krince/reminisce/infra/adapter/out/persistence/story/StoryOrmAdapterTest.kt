package com.krince.reminisce.infra.adapter.out.persistence.story

import com.krince.reminisce.domain.model.story.vo.PostActivityConfig
import com.krince.reminisce.domain.model.story.vo.SceneType
import com.krince.reminisce.domain.model.story.vo.StoryId
import com.krince.reminisce.domain.model.story.vo.StoryStatus
import com.krince.reminisce.domain.model.story.vo.ThinkingElement
import com.krince.reminisce.infra.adapter.out.persistence.story.entity.SceneOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.story.entity.StoryOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.story.entity.StoryTopicOrmEntity
import com.krince.reminisce.testutil.TestConfig
import com.krince.reminisce.testutil.fixture.TestStoryFixture
import io.kotest.core.annotation.DisplayName
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("localtest")
@Import(TestConfig::class)
@io.kotest.core.annotation.Tags("test", "integrationTest")
@DisplayName("StoryOrmAdapter 통합테스트")
class StoryOrmAdapterTest(
    private val storyOrmAdapter: StoryOrmAdapter,
    private val testStoryFixture: TestStoryFixture,
) : FunSpec({

    val postActivityConfig = PostActivityConfig(
        cards = listOf(
            PostActivityConfig.Card(id = "card_1", text = "며느리가 방귀를 참았어요.", correctOrder = 1),
            PostActivityConfig.Card(id = "card_2", text = "방귀가 크게 터졌어요.", correctOrder = 2),
        ),
        retellingKeywords = listOf("며느리", "방귀", "배나무"),
    )

    fun uniqueSuffix(): String = "${System.currentTimeMillis()}-${System.nanoTime()}"

    fun storyEntity(
        storyId: String,
        status: String = StoryStatus.PUBLISHED.name,
        postActivityConfigValue: PostActivityConfig? = null,
    ): StoryOrmEntity = StoryOrmEntity(
        storyId = storyId,
        title = "제목-$storyId",
        summary = "요약-$storyId",
        intro = "도입-$storyId",
        situation = "상황-$storyId",
        childRole = "역할-$storyId",
        difficulty = "보통",
        estimatedMinutes = 20,
        representativeImageUrl = "/files/$storyId.png",
        status = status,
        postActivityConfig = postActivityConfigValue,
    )

    fun narrationEntity(sceneId: String, storyId: String, sceneOrder: Short, chapter: Short = 1): SceneOrmEntity = SceneOrmEntity(
        sceneId = sceneId,
        storyId = storyId,
        sceneOrder = sceneOrder,
        chapter = chapter,
        sceneType = SceneType.NARRATION.name,
        sceneDescription = "전개 설명 $sceneOrder",
        characterName = null,
        characterDisplayName = null,
        characterOpening = null,
        characterClosing = null,
        conflict = null,
        sceneGoal = null,
        requiredElements = null,
        preferredTurns = null,
        maxTurns = null,
    )

    fun dialogueEntity(sceneId: String, storyId: String, sceneOrder: Short, chapter: Short = 1): SceneOrmEntity = SceneOrmEntity(
        sceneId = sceneId,
        storyId = storyId,
        sceneOrder = sceneOrder,
        chapter = chapter,
        sceneType = SceneType.DIALOGUE.name,
        sceneDescription = "대화 설명 $sceneOrder",
        characterName = "ch_banggui_daughter_in_law",
        characterDisplayName = "방귀쟁이 며느리",
        characterOpening = "고정 첫 대사",
        characterClosing = "고정 마지막 대사",
        conflict = null,
        sceneGoal = "장면 발화 목표",
        requiredElements = listOf(
            ThinkingElement.PERSPECTIVE,
            ThinkingElement.EMOTION,
            ThinkingElement.REASON,
            ThinkingElement.SOLUTION,
        ),
        preferredTurns = null,
        maxTurns = 4,
    )

    fun topicEntity(storyId: String, topic: String): StoryTopicOrmEntity = StoryTopicOrmEntity(
        id = "topic-$topic-$storyId",
        storyId = storyId,
        topic = topic,
    )

    beforeTest {
        testStoryFixture.deleteAllBatch()
    }

    context("findAllPublished") {
        context("성공") {
            test("공개 이야기만 주제와 함께 반환하고 draft는 제외한다") {
                val publishedId = "published-${uniqueSuffix()}"
                val draftId = "draft-${uniqueSuffix()}"
                testStoryFixture.saveStory(storyEntity(publishedId))
                testStoryFixture.saveStory(storyEntity(draftId, status = StoryStatus.DRAFT.name))
                testStoryFixture.saveTopic(topicEntity(publishedId, "다름"))
                testStoryFixture.saveTopic(topicEntity(publishedId, "자기이해"))
                testStoryFixture.saveTopic(topicEntity(draftId, "다름"))

                val results = storyOrmAdapter.findAllPublished()

                results shouldHaveSize 1
                results.first().storyId shouldBe StoryId(publishedId)
                results.first().topics shouldContainExactlyInAnyOrder listOf("다름", "자기이해")
                results.first().scenes.shouldBeEmpty()
            }
        }
    }

    context("findAllPublishedByTopic") {
        context("성공") {
            test("해당 주제를 가진 공개 이야기만 반환한다") {
                val matchedId = "matched-${uniqueSuffix()}"
                val unmatchedId = "unmatched-${uniqueSuffix()}"
                val draftId = "draft-${uniqueSuffix()}"
                testStoryFixture.saveStory(storyEntity(matchedId))
                testStoryFixture.saveStory(storyEntity(unmatchedId))
                testStoryFixture.saveStory(storyEntity(draftId, status = StoryStatus.DRAFT.name))
                testStoryFixture.saveTopic(topicEntity(matchedId, "다름"))
                testStoryFixture.saveTopic(topicEntity(unmatchedId, "용기"))
                testStoryFixture.saveTopic(topicEntity(draftId, "다름"))

                val results = storyOrmAdapter.findAllPublishedByTopic("다름")

                results shouldHaveSize 1
                results.first().storyId shouldBe StoryId(matchedId)
                results.first().topics shouldContainExactly listOf("다름")
            }

            test("해당 주제를 가진 이야기가 없으면 빈 목록을 반환한다") {
                val publishedId = "published-${uniqueSuffix()}"
                testStoryFixture.saveStory(storyEntity(publishedId))
                testStoryFixture.saveTopic(topicEntity(publishedId, "다름"))

                storyOrmAdapter.findAllPublishedByTopic("없는주제").shouldBeEmpty()
            }
        }
    }

    context("findByIdWithScenesPublished") {
        context("성공") {
            test("장면을 순서대로 조립하고 requiredElements와 postActivityConfig를 왕복 보존한다") {
                val storyId = "detail-${uniqueSuffix()}"
                testStoryFixture.saveStory(storyEntity(storyId, postActivityConfigValue = postActivityConfig))
                testStoryFixture.saveScene(dialogueEntity("sc-3-$storyId", storyId, 3, chapter = 2))
                testStoryFixture.saveScene(narrationEntity("sc-1-$storyId", storyId, 1, chapter = 1))
                testStoryFixture.saveScene(narrationEntity("sc-2-$storyId", storyId, 2, chapter = 1))
                testStoryFixture.saveTopic(topicEntity(storyId, "다름"))

                val result = storyOrmAdapter.findByIdWithScenesPublished(StoryId(storyId))

                result.shouldNotBeNull()
                result.intro shouldBe "도입-$storyId"
                result.situation shouldBe "상황-$storyId"
                result.childRole shouldBe "역할-$storyId"
                result.postActivityConfig shouldBe postActivityConfig
                result.topics shouldContainExactly listOf("다름")
                result.scenes.map { it.sceneOrder } shouldContainExactly listOf(1, 2, 3)
                result.scenes.map { it.chapter } shouldContainExactly listOf(1, 1, 2)

                val dialogue = result.scenes[2]
                dialogue.sceneType shouldBe SceneType.DIALOGUE
                dialogue.characterName shouldBe "ch_banggui_daughter_in_law"
                dialogue.characterDisplayName shouldBe "방귀쟁이 며느리"
                dialogue.characterOpening shouldBe "고정 첫 대사"
                dialogue.characterClosing shouldBe "고정 마지막 대사"
                dialogue.sceneGoal shouldBe "장면 발화 목표"
                val requiredElements = dialogue.requiredElements.shouldNotBeNull()
                requiredElements shouldContainExactly listOf(
                    ThinkingElement.PERSPECTIVE,
                    ThinkingElement.EMOTION,
                    ThinkingElement.REASON,
                    ThinkingElement.SOLUTION,
                )
                dialogue.maxTurns shouldBe 4
            }
        }
        context("실패") {
            test("draft 이야기 id로 조회하면 null을 반환한다") {
                val draftId = "draft-${uniqueSuffix()}"
                testStoryFixture.saveStory(storyEntity(draftId, status = StoryStatus.DRAFT.name))

                storyOrmAdapter.findByIdWithScenesPublished(StoryId(draftId)) shouldBe null
            }

            test("존재하지 않는 이야기 id로 조회하면 null을 반환한다") {
                storyOrmAdapter.findByIdWithScenesPublished(StoryId("unknown-${uniqueSuffix()}")) shouldBe null
            }
        }
    }
})
