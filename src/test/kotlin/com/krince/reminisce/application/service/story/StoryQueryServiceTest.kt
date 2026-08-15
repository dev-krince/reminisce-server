package com.krince.reminisce.application.service.story

import com.krince.reminisce.application.port.access.child.ChildAccessPort
import com.krince.reminisce.application.port.access.savedstory.SavedStoryAccessPort
import com.krince.reminisce.application.port.`in`.story.command.GetStoriesCommand
import com.krince.reminisce.application.port.`in`.story.command.GetStoryCommand
import com.krince.reminisce.application.port.out.story.LoadStoryPort
import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.story.CharacterVoice
import com.krince.reminisce.domain.model.story.Scene
import com.krince.reminisce.domain.model.story.Story
import com.krince.reminisce.domain.model.story.VoiceAgeGroup
import com.krince.reminisce.domain.model.story.VoiceGender
import com.krince.reminisce.domain.model.story.vo.Difficulty
import com.krince.reminisce.domain.model.story.vo.PostActivityConfig
import com.krince.reminisce.domain.model.story.vo.SceneId
import com.krince.reminisce.domain.model.story.vo.SceneType
import com.krince.reminisce.domain.model.story.vo.StoryGenre
import com.krince.reminisce.domain.model.story.vo.StoryId
import com.krince.reminisce.domain.model.story.vo.StorySort
import com.krince.reminisce.domain.model.story.vo.StoryStatus
import com.krince.reminisce.domain.model.story.vo.ThinkingElement
import com.krince.reminisce.domain.model.user.vo.UserId
import com.krince.reminisce.shared.exception.NotFoundException
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND_STORY
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

@Tags("test", "unitTest")
@DisplayName("StoryQueryService 단위테스트")
class StoryQueryServiceTest : FunSpec({

    val loadStoryPort = mockk<LoadStoryPort>()
    val savedStoryAccessPort = mockk<SavedStoryAccessPort>()
    val childAccessPort = mockk<ChildAccessPort>()
    val service = StoryQueryService(loadStoryPort, savedStoryAccessPort, childAccessPort)

    beforeEach { clearAllMocks() }

    val storyIdStr = "story-uuid-1"
    val postActivityConfig = PostActivityConfig(
        cards = listOf(PostActivityConfig.Card(id = "card_1", text = "카드 내용", correctOrder = 1)),
        retellingKeywords = listOf("며느리", "방귀"),
    )

    fun narrationScene(sceneId: String, sceneOrder: Int): Scene = Scene(
        sceneId = SceneId(sceneId),
        storyId = StoryId(storyIdStr),
        sceneOrder = sceneOrder,
        sceneType = SceneType.NARRATION,
        sceneDescription = "전개 설명 $sceneOrder",
    )

    fun dialogueScene(sceneId: String, sceneOrder: Int): Scene = Scene(
        sceneId = SceneId(sceneId),
        storyId = StoryId(storyIdStr),
        sceneOrder = sceneOrder,
        sceneType = SceneType.DIALOGUE,
        sceneDescription = "대화 설명 $sceneOrder",
        characterName = "ch_banggui_daughter_in_law",
        characterDisplayName = "방귀쟁이 며느리",
        sceneGoal = "장면 발화 목표",
        requiredElements = listOf(ThinkingElement.PERSPECTIVE, ThinkingElement.EMOTION),
        maxTurns = 4,
        characterVoice = CharacterVoice(
            gender = VoiceGender.FEMALE,
            ageGroup = VoiceAgeGroup.ADULT,
            voiceProfile = "young_woman_gentle",
        ),
        characterImageUrl = "/files/char-ch_banggui_daughter_in_law.png",
    )

    fun story(scenes: List<Scene>): Story = Story(
        storyId = StoryId(storyIdStr),
        title = "방귀 뀌는 며느리",
        summary = "이야기 요약",
        intro = "이야기 도입",
        situation = "이야기 상황",
        childRole = "아이 역할",
        difficulty = Difficulty("보통"),
        estimatedMinutes = 20,
        representativeImageUrl = "/files/story.png",
        status = StoryStatus.PUBLISHED,
        postActivityConfig = postActivityConfig,
        topics = listOf("다름", "자기이해"),
        genre = StoryGenre.FOLKTALE,
        scenes = scenes,
    )

    context("GetStoriesUseCase") {
        context("성공") {
            test("파라미터 미지정이면 필터 없이 RECOMMENDED로 전체 공개 이야기를 요약 결과로 반환한다") {
                every {
                    loadStoryPort.findPublished(null, null, null, StorySort.RECOMMENDED)
                } returns listOf(story(scenes = emptyList()))

                val results = service.execute(
                    GetStoriesCommand(topic = null, genre = null, titleKeyword = null, sort = StorySort.RECOMMENDED),
                )

                results shouldHaveSize 1
                results.first().storyId shouldBe storyIdStr
                results.first().title shouldBe "방귀 뀌는 며느리"
                results.first().representativeImageUrl shouldBe "/files/story.png"
                results.first().estimatedMinutes shouldBe 20
                results.first().topics shouldContainExactly listOf("다름", "자기이해")
                results.first().genre shouldBe "전래동화"
                results.first().difficulty shouldBe "보통"
                results.first().isBookmarked shouldBe false
                verify(exactly = 1) { loadStoryPort.findPublished(null, null, null, StorySort.RECOMMENDED) }
                verify(exactly = 0) { savedStoryAccessPort.findBookmarkedStoryIds(any()) }
            }

            test("genre만 지정하면 genre 인자로 findPublished에 위임한다") {
                every {
                    loadStoryPort.findPublished(StoryGenre.FOLKTALE, null, null, StorySort.RECOMMENDED)
                } returns listOf(story(scenes = emptyList()))

                val results = service.execute(
                    GetStoriesCommand(
                        topic = null,
                        genre = StoryGenre.FOLKTALE,
                        titleKeyword = null,
                        sort = StorySort.RECOMMENDED,
                    ),
                )

                results shouldHaveSize 1
                verify(exactly = 1) {
                    loadStoryPort.findPublished(StoryGenre.FOLKTALE, null, null, StorySort.RECOMMENDED)
                }
            }

            test("q(제목검색어)를 titleKeyword 인자로 findPublished에 위임한다") {
                every {
                    loadStoryPort.findPublished(null, null, "며느리", StorySort.RECOMMENDED)
                } returns listOf(story(scenes = emptyList()))

                val results = service.execute(
                    GetStoriesCommand(topic = null, genre = null, titleKeyword = "며느리", sort = StorySort.RECOMMENDED),
                )

                results shouldHaveSize 1
                verify(exactly = 1) { loadStoryPort.findPublished(null, null, "며느리", StorySort.RECOMMENDED) }
            }

            test("topic을 topic 인자로 findPublished에 위임한다") {
                every {
                    loadStoryPort.findPublished(null, "다름", null, StorySort.RECOMMENDED)
                } returns listOf(story(scenes = emptyList()))

                val results = service.execute(
                    GetStoriesCommand(topic = "다름", genre = null, titleKeyword = null, sort = StorySort.RECOMMENDED),
                )

                results shouldHaveSize 1
                verify(exactly = 1) { loadStoryPort.findPublished(null, "다름", null, StorySort.RECOMMENDED) }
            }

            test("genre·q·topic·sort를 모두 담아 그대로 findPublished에 위임한다") {
                every {
                    loadStoryPort.findPublished(StoryGenre.CREATIVE, "용기", "곰", StorySort.LATEST)
                } returns listOf(story(scenes = emptyList()))

                val results = service.execute(
                    GetStoriesCommand(
                        topic = "용기",
                        genre = StoryGenre.CREATIVE,
                        titleKeyword = "곰",
                        sort = StorySort.LATEST,
                    ),
                )

                results shouldHaveSize 1
                verify(exactly = 1) {
                    loadStoryPort.findPublished(StoryGenre.CREATIVE, "용기", "곰", StorySort.LATEST)
                }
            }

            test("공개 이야기가 없으면 빈 목록을 반환한다") {
                every {
                    loadStoryPort.findPublished(null, null, null, StorySort.RECOMMENDED)
                } returns emptyList()

                val results = service.execute(
                    GetStoriesCommand(topic = null, genre = null, titleKeyword = null, sort = StorySort.RECOMMENDED),
                )

                results shouldHaveSize 0
            }
        }

        context("찜 여부(isBookmarked)") {
            val childIdStr = "child-uuid-1"
            val guardianIdStr = "guardian-uuid-1"

            fun storyWithId(id: String): Story = Story(
                storyId = StoryId(id),
                title = "제목-$id",
                summary = "이야기 요약",
                intro = "이야기 도입",
                situation = "이야기 상황",
                childRole = "아이 역할",
                difficulty = Difficulty("보통"),
                estimatedMinutes = 20,
                representativeImageUrl = "/files/$id.png",
                status = StoryStatus.PUBLISHED,
                postActivityConfig = postActivityConfig,
                topics = listOf("다름"),
                genre = StoryGenre.FOLKTALE,
                scenes = emptyList(),
            )

            test("childId가 없으면 소유권·찜 조회 없이 모든 항목 isBookmarked=false") {
                every {
                    loadStoryPort.findPublished(null, null, null, StorySort.RECOMMENDED)
                } returns listOf(storyWithId("s_a"), storyWithId("s_b"))

                val results = service.execute(
                    GetStoriesCommand(topic = null, genre = null, titleKeyword = null, sort = StorySort.RECOMMENDED),
                )

                results.map { it.isBookmarked } shouldContainExactly listOf(false, false)
                verify(exactly = 0) { childAccessPort.findGuardianId(any()) }
                verify(exactly = 0) { savedStoryAccessPort.findBookmarkedStoryIds(any()) }
            }

            test("childId를 주면 그 아이가 찜한 이야기만 isBookmarked=true로 표시한다") {
                every {
                    loadStoryPort.findPublished(null, null, null, StorySort.RECOMMENDED)
                } returns listOf(storyWithId("s_a"), storyWithId("s_b"))
                every { childAccessPort.findGuardianId(ChildId(childIdStr)) } returns UserId(guardianIdStr)
                every { savedStoryAccessPort.findBookmarkedStoryIds(ChildId(childIdStr)) } returns setOf("s_b")

                val results = service.execute(
                    GetStoriesCommand(
                        topic = null,
                        genre = null,
                        titleKeyword = null,
                        sort = StorySort.RECOMMENDED,
                        childId = childIdStr,
                        guardianId = guardianIdStr,
                    ),
                )

                results.single { it.storyId == "s_a" }.isBookmarked shouldBe false
                results.single { it.storyId == "s_b" }.isBookmarked shouldBe true
            }

            test("childId가 남의 아이면 NotFoundException(NOT_FOUND)을 던지고 찜 조회를 하지 않는다") {
                every {
                    loadStoryPort.findPublished(null, null, null, StorySort.RECOMMENDED)
                } returns listOf(storyWithId("s_a"))
                every { childAccessPort.findGuardianId(ChildId(childIdStr)) } returns UserId("guardian-uuid-2")

                val exception = shouldThrow<NotFoundException> {
                    service.execute(
                        GetStoriesCommand(
                            topic = null,
                            genre = null,
                            titleKeyword = null,
                            sort = StorySort.RECOMMENDED,
                            childId = childIdStr,
                            guardianId = guardianIdStr,
                        ),
                    )
                }

                exception.exceptionResponseCode shouldBe NOT_FOUND
                verify(exactly = 0) { savedStoryAccessPort.findBookmarkedStoryIds(any()) }
            }
        }
    }

    context("GetStoryUseCase") {
        context("성공") {
            test("이야기 상세를 장면 순서와 타입별 필드 그대로 조립한다") {
                every { loadStoryPort.findByIdWithScenesPublished(StoryId(storyIdStr)) } returns story(
                    scenes = listOf(
                        narrationScene("sc-1", 1),
                        dialogueScene("sc-2", 2),
                    ),
                )

                val result = service.execute(GetStoryCommand(storyId = storyIdStr))

                result.storyId shouldBe storyIdStr
                result.title shouldBe "방귀 뀌는 며느리"
                result.intro shouldBe "이야기 도입"
                result.situation shouldBe "이야기 상황"
                result.childRole shouldBe "아이 역할"
                result.difficulty shouldBe "보통"
                result.topics shouldContainExactly listOf("다름", "자기이해")
                val postActivity = result.postActivity.shouldNotBeNull()
                postActivity.cards.map { it.id } shouldContainExactly listOf("card_1")
                postActivity.retellingKeywords shouldContainExactly listOf("며느리", "방귀")
                result.scenes.map { it.sceneOrder } shouldContainExactly listOf(1, 2)

                val narration = result.scenes[0]
                narration.sceneType shouldBe SceneType.NARRATION
                narration.sceneDescription shouldBe "전개 설명 1"
                narration.characterName shouldBe null

                val dialogue = result.scenes[1]
                dialogue.sceneType shouldBe SceneType.DIALOGUE
                dialogue.characterName shouldBe "ch_banggui_daughter_in_law"
                dialogue.characterDisplayName shouldBe "방귀쟁이 며느리"
                dialogue.characterOpening shouldBe null
                dialogue.characterClosing shouldBe null
                dialogue.sceneGoal shouldBe "장면 발화 목표"
                val requiredElements = dialogue.requiredElements.shouldNotBeNull()
                requiredElements shouldContainExactly listOf(
                    ThinkingElement.PERSPECTIVE,
                    ThinkingElement.EMOTION,
                )
                dialogue.maxTurns shouldBe 4
            }
        }
        context("실패") {
            test("공개 이야기가 없으면 NOT_FOUND_STORY로 NotFoundException을 던진다") {
                every { loadStoryPort.findByIdWithScenesPublished(StoryId("unknown-story")) } returns null

                val exception = shouldThrow<NotFoundException> {
                    service.execute(GetStoryCommand(storyId = "unknown-story"))
                }

                exception.exceptionResponseCode shouldBe NOT_FOUND_STORY
            }
        }
    }
})
