package com.krince.reminisce.infra.adapter.out.persistence.story.mapper

import com.krince.reminisce.domain.model.story.CharacterVoice
import com.krince.reminisce.domain.model.story.Mission
import com.krince.reminisce.domain.model.story.Scene
import com.krince.reminisce.domain.model.story.Story
import com.krince.reminisce.domain.model.story.VoiceAgeGroup
import com.krince.reminisce.domain.model.story.VoiceGender
import com.krince.reminisce.domain.model.story.vo.Difficulty
import com.krince.reminisce.domain.model.story.vo.PostActivityConfig
import com.krince.reminisce.domain.model.story.vo.SceneId
import com.krince.reminisce.domain.model.story.vo.SceneType
import com.krince.reminisce.domain.model.story.vo.StoryId
import com.krince.reminisce.domain.model.story.vo.StoryStatus
import com.krince.reminisce.domain.model.story.vo.ThinkingElement
import com.krince.reminisce.infra.adapter.out.persistence.story.dto.StoryAggregateEntity
import com.krince.reminisce.infra.adapter.out.persistence.story.entity.SceneOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.story.entity.StoryOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.story.entity.StoryTopicOrmEntity
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeBlank
import java.time.LocalDateTime

@Tags("test", "unitTest")
@DisplayName("StoryMapper 단위테스트")
class StoryMapperTest : FunSpec({

    val storyIdStr = "story-uuid-1"
    val createdDate = LocalDateTime.of(2026, 8, 1, 10, 0)
    val modifiedDate = LocalDateTime.of(2026, 8, 2, 10, 0)
    val postActivityConfig = PostActivityConfig(
        cards = listOf(PostActivityConfig.Card(id = "card_1", text = "카드 내용", correctOrder = 1)),
        retellingKeywords = listOf("며느리", "방귀"),
    )

    fun storyOrmEntity(): StoryOrmEntity = StoryOrmEntity(
        storyId = storyIdStr,
        title = "방귀 뀌는 며느리",
        summary = "이야기 요약",
        intro = "이야기 도입",
        situation = "이야기 상황",
        childRole = "아이 역할",
        difficulty = "보통",
        estimatedMinutes = 20,
        representativeImageUrl = "/files/story.png",
        status = "PUBLISHED",
        postActivityConfig = postActivityConfig,
    ).apply {
        this.createdDate = createdDate
        this.modifiedDate = modifiedDate
    }

    fun narrationOrmEntity(sceneId: String, sceneOrder: Short): SceneOrmEntity = SceneOrmEntity(
        sceneId = sceneId,
        storyId = storyIdStr,
        sceneOrder = sceneOrder,
        sceneType = "NARRATION",
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

    fun dialogueOrmEntity(sceneId: String, sceneOrder: Short): SceneOrmEntity = SceneOrmEntity(
        sceneId = sceneId,
        storyId = storyIdStr,
        sceneOrder = sceneOrder,
        sceneType = "DIALOGUE",
        sceneDescription = "대화 설명 $sceneOrder",
        characterName = "ch_banggui_daughter_in_law",
        characterDisplayName = "방귀쟁이 며느리",
        characterOpening = "고정 첫 대사",
        characterClosing = "고정 마지막 대사",
        conflict = null,
        sceneGoal = "장면 발화 목표",
        requiredElements = listOf(ThinkingElement.PERSPECTIVE, ThinkingElement.EMOTION),
        preferredTurns = null,
        maxTurns = 4,
    )

    fun dialogueEntityWithCharacterVoice(sceneId: String, sceneOrder: Short, characterVoice: CharacterVoice): SceneOrmEntity = SceneOrmEntity(
        sceneId = sceneId,
        storyId = storyIdStr,
        sceneOrder = sceneOrder,
        sceneType = "DIALOGUE",
        sceneDescription = "대화 설명 $sceneOrder",
        characterName = "ch_banggui_daughter_in_law",
        characterDisplayName = "방귀쟁이 며느리",
        characterOpening = "고정 첫 대사",
        characterClosing = "고정 마지막 대사",
        conflict = null,
        sceneGoal = "장면 발화 목표",
        requiredElements = listOf(ThinkingElement.PERSPECTIVE, ThinkingElement.EMOTION),
        preferredTurns = null,
        maxTurns = 4,
        characterVoice = characterVoice,
    )

    context("toDomain") {
        context("성공") {
            test("뒤섞인 장면 엔티티를 sceneOrder 오름차순 도메인으로 조립하고 전 필드를 보존한다") {
                val aggregateEntity = StoryAggregateEntity(
                    storyOrmEntity = storyOrmEntity(),
                    sceneOrmEntities = listOf(
                        dialogueOrmEntity("sc-3", 3),
                        narrationOrmEntity("sc-1", 1),
                        narrationOrmEntity("sc-2", 2),
                    ),
                    storyTopicOrmEntities = listOf(
                        StoryTopicOrmEntity(id = "topic-1", storyId = storyIdStr, topic = "다름"),
                        StoryTopicOrmEntity(id = "topic-2", storyId = storyIdStr, topic = "자기이해"),
                    ),
                )

                val story = StoryMapper.toDomain(aggregateEntity)

                story.storyId shouldBe StoryId(storyIdStr)
                story.title shouldBe "방귀 뀌는 며느리"
                story.summary shouldBe "이야기 요약"
                story.intro shouldBe "이야기 도입"
                story.situation shouldBe "이야기 상황"
                story.childRole shouldBe "아이 역할"
                story.difficulty shouldBe Difficulty("보통")
                story.estimatedMinutes shouldBe 20
                story.representativeImageUrl shouldBe "/files/story.png"
                story.status shouldBe StoryStatus.PUBLISHED
                story.postActivityConfig shouldBe postActivityConfig
                story.topics shouldContainExactly listOf("다름", "자기이해")
                story.createdDate shouldBe createdDate
                story.modifiedDate shouldBe modifiedDate
                story.scenes.map { it.sceneId.value } shouldContainExactly listOf("sc-1", "sc-2", "sc-3")
                story.scenes.map { it.sceneOrder } shouldContainExactly listOf(1, 2, 3)
            }

            test("대화 장면 엔티티의 대화 전용 필드를 전부 도메인으로 옮긴다") {
                val aggregateEntity = StoryAggregateEntity(
                    storyOrmEntity = storyOrmEntity(),
                    sceneOrmEntities = listOf(dialogueOrmEntity("sc-3", 3)),
                    storyTopicOrmEntities = emptyList(),
                )

                val dialogue = StoryMapper.toDomain(aggregateEntity).scenes.first()

                dialogue.sceneType shouldBe SceneType.DIALOGUE
                dialogue.sceneDescription shouldBe "대화 설명 3"
                dialogue.characterName shouldBe "ch_banggui_daughter_in_law"
                dialogue.characterDisplayName shouldBe "방귀쟁이 며느리"
                dialogue.characterOpening shouldBe "고정 첫 대사"
                dialogue.characterClosing shouldBe "고정 마지막 대사"
                dialogue.conflict shouldBe null
                dialogue.sceneGoal shouldBe "장면 발화 목표"
                dialogue.requiredElements shouldContainExactly listOf(
                    ThinkingElement.PERSPECTIVE,
                    ThinkingElement.EMOTION,
                )
                dialogue.preferredTurns shouldBe null
                dialogue.maxTurns shouldBe 4
                dialogue.mission shouldBe null
            }

            test("미션이 있는 대화 장면 엔티티의 mission을 도메인으로 옮긴다") {
                val mission = Mission(goal = "배 따기 방법 찾기", examples = listOf("무엇을 사용할 것인지"))
                val entityWithMission = SceneOrmEntity(
                    sceneId = "sc-7",
                    storyId = storyIdStr,
                    sceneOrder = 7,
                    sceneType = "DIALOGUE",
                    sceneDescription = "대화 설명 7",
                    characterName = "ch_banggui_village_chief",
                    characterDisplayName = "마을 이장",
                    characterOpening = "고정 첫 대사",
                    characterClosing = "고정 마지막 대사",
                    conflict = null,
                    sceneGoal = "장면 발화 목표",
                    requiredElements = listOf(ThinkingElement.SOLUTION),
                    preferredTurns = null,
                    maxTurns = 5,
                    mission = mission,
                )
                val aggregateEntity = StoryAggregateEntity(
                    storyOrmEntity = storyOrmEntity(),
                    sceneOrmEntities = listOf(entityWithMission),
                    storyTopicOrmEntities = emptyList(),
                )

                val dialogue = StoryMapper.toDomain(aggregateEntity).scenes.first()

                dialogue.mission shouldBe mission
            }

            test("음성 메타가 있는 대화 장면 엔티티의 characterVoice를 도메인으로 옮긴다") {
                val voice = CharacterVoice(
                    gender = VoiceGender.FEMALE,
                    ageGroup = VoiceAgeGroup.ADULT,
                    voiceProfile = "young_woman_gentle",
                )
                val aggregateEntity = StoryAggregateEntity(
                    storyOrmEntity = storyOrmEntity(),
                    sceneOrmEntities = listOf(dialogueEntityWithCharacterVoice("sc-3", 3, voice)),
                    storyTopicOrmEntities = emptyList(),
                )

                val dialogue = StoryMapper.toDomain(aggregateEntity).scenes.first()

                dialogue.characterVoice shouldBe voice
            }
        }
    }

    context("toEntity") {
        context("성공") {
            test("도메인 이야기를 이야기·장면·주제 엔티티로 분해하고 전 필드를 보존한다") {
                val story = Story(
                    storyId = StoryId(storyIdStr),
                    title = "방귀 뀌는 며느리",
                    summary = "이야기 요약",
                    intro = "이야기 도입",
                    situation = null,
                    childRole = null,
                    difficulty = Difficulty("보통"),
                    estimatedMinutes = 20,
                    representativeImageUrl = null,
                    status = StoryStatus.PUBLISHED,
                    postActivityConfig = postActivityConfig,
                    topics = listOf("다름", "자기이해"),
                    scenes = listOf(
                        Scene(
                            sceneId = SceneId("sc-1"),
                            storyId = StoryId(storyIdStr),
                            sceneOrder = 1,
                            sceneType = SceneType.NARRATION,
                            sceneDescription = "전개 설명 1",
                        ),
                        Scene(
                            sceneId = SceneId("sc-2"),
                            storyId = StoryId(storyIdStr),
                            sceneOrder = 2,
                            sceneType = SceneType.DIALOGUE,
                            sceneDescription = "대화 설명 2",
                            characterName = "ch_banggui_father_in_law",
                            characterDisplayName = "시아버지",
                            characterOpening = "고정 첫 대사",
                            characterClosing = "고정 마지막 대사",
                            sceneGoal = "장면 발화 목표",
                            requiredElements = listOf(ThinkingElement.REASON, ThinkingElement.SOLUTION),
                            maxTurns = 5,
                        ),
                    ),
                )

                val aggregateEntity = StoryMapper.toEntity(story)

                val savedStory = aggregateEntity.storyOrmEntity
                savedStory.storyId shouldBe storyIdStr
                savedStory.title shouldBe "방귀 뀌는 며느리"
                savedStory.summary shouldBe "이야기 요약"
                savedStory.intro shouldBe "이야기 도입"
                savedStory.situation shouldBe null
                savedStory.childRole shouldBe null
                savedStory.difficulty shouldBe "보통"
                savedStory.estimatedMinutes shouldBe 20.toShort()
                savedStory.representativeImageUrl shouldBe null
                savedStory.status shouldBe "PUBLISHED"
                savedStory.postActivityConfig shouldBe postActivityConfig

                val savedScenes = aggregateEntity.sceneOrmEntities
                savedScenes.map { it.sceneId } shouldContainExactly listOf("sc-1", "sc-2")
                savedScenes.map { it.storyId } shouldContainExactly listOf(storyIdStr, storyIdStr)
                savedScenes.map { it.sceneType } shouldContainExactly listOf("NARRATION", "DIALOGUE")
                savedScenes[1].characterName shouldBe "ch_banggui_father_in_law"
                savedScenes[1].characterDisplayName shouldBe "시아버지"
                savedScenes[1].characterOpening shouldBe "고정 첫 대사"
                savedScenes[1].characterClosing shouldBe "고정 마지막 대사"
                savedScenes[1].sceneGoal shouldBe "장면 발화 목표"
                savedScenes[1].requiredElements shouldContainExactly listOf(
                    ThinkingElement.REASON,
                    ThinkingElement.SOLUTION,
                )
                savedScenes[1].preferredTurns shouldBe null
                savedScenes[1].maxTurns shouldBe 5.toShort()
                savedScenes[1].mission shouldBe null
                savedScenes[1].characterVoice shouldBe null

                val savedTopics = aggregateEntity.storyTopicOrmEntities
                savedTopics.map { it.topic } shouldContainExactly listOf("다름", "자기이해")
                savedTopics.forEach { savedTopic ->
                    savedTopic.storyId shouldBe storyIdStr
                    savedTopic.id.shouldNotBeBlank()
                }
            }
        }
    }

    context("왕복") {
        context("성공") {
            test("toDomain 후 toEntity 하면 이야기·장면·주제 값이 그대로 보존된다") {
                val aggregateEntity = StoryAggregateEntity(
                    storyOrmEntity = storyOrmEntity(),
                    sceneOrmEntities = listOf(narrationOrmEntity("sc-1", 1), dialogueOrmEntity("sc-2", 2)),
                    storyTopicOrmEntities = listOf(
                        StoryTopicOrmEntity(id = "topic-1", storyId = storyIdStr, topic = "다름"),
                    ),
                )

                val restored = StoryMapper.toEntity(StoryMapper.toDomain(aggregateEntity))

                restored.storyOrmEntity.storyId shouldBe storyIdStr
                restored.storyOrmEntity.status shouldBe "PUBLISHED"
                restored.storyOrmEntity.postActivityConfig shouldBe postActivityConfig
                restored.storyOrmEntity.createdDate shouldBe createdDate
                restored.storyOrmEntity.modifiedDate shouldBe modifiedDate
                restored.sceneOrmEntities.map { it.sceneId } shouldContainExactly listOf("sc-1", "sc-2")
                restored.sceneOrmEntities[1].requiredElements shouldContainExactly listOf(
                    ThinkingElement.PERSPECTIVE,
                    ThinkingElement.EMOTION,
                )
                restored.sceneOrmEntities[1].mission shouldBe null
                restored.storyTopicOrmEntities.map { it.topic } shouldContainExactly listOf("다름")
            }

            test("미션이 있는 대화 장면도 toDomain 후 toEntity 하면 mission이 보존된다") {
                val mission = Mission(goal = "배 따기 방법 찾기", examples = listOf("무엇을 사용할 것인지"))
                val entityWithMission = SceneOrmEntity(
                    sceneId = "sc-7",
                    storyId = storyIdStr,
                    sceneOrder = 7,
                    sceneType = "DIALOGUE",
                    sceneDescription = "대화 설명 7",
                    characterName = "ch_banggui_village_chief",
                    characterDisplayName = "마을 이장",
                    characterOpening = "고정 첫 대사",
                    characterClosing = "고정 마지막 대사",
                    conflict = null,
                    sceneGoal = "장면 발화 목표",
                    requiredElements = listOf(ThinkingElement.SOLUTION),
                    preferredTurns = null,
                    maxTurns = 5,
                    mission = mission,
                )
                val aggregateEntity = StoryAggregateEntity(
                    storyOrmEntity = storyOrmEntity(),
                    sceneOrmEntities = listOf(entityWithMission),
                    storyTopicOrmEntities = emptyList(),
                )

                val restored = StoryMapper.toEntity(StoryMapper.toDomain(aggregateEntity))

                restored.sceneOrmEntities.first().mission shouldBe mission
            }

            test("음성 메타가 있는 대화 장면도 toDomain 후 toEntity 하면 characterVoice가 보존된다") {
                val voice = CharacterVoice(
                    gender = VoiceGender.FEMALE,
                    ageGroup = VoiceAgeGroup.ADULT,
                    voiceProfile = "young_woman_gentle",
                )
                val aggregateEntity = StoryAggregateEntity(
                    storyOrmEntity = storyOrmEntity(),
                    sceneOrmEntities = listOf(dialogueEntityWithCharacterVoice("sc-3", 3, voice)),
                    storyTopicOrmEntities = emptyList(),
                )

                val restored = StoryMapper.toEntity(StoryMapper.toDomain(aggregateEntity))

                restored.sceneOrmEntities.first().characterVoice shouldBe voice
            }
        }
    }
})
