package com.krince.reminisce.infra.adapter.`in`.controller

import com.krince.reminisce.domain.model.story.Mission
import com.krince.reminisce.domain.model.story.vo.PostActivityConfig
import com.krince.reminisce.domain.model.story.vo.SceneType
import com.krince.reminisce.domain.model.story.vo.StoryStatus
import com.krince.reminisce.domain.model.story.vo.ThinkingElement
import com.krince.reminisce.infra.adapter.out.persistence.child.entity.ChildOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.speakingsession.entity.SpeakingSessionOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.story.entity.SceneOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.story.entity.StoryOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.story.entity.StoryTopicOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.user.entity.UserOrmEntity
import com.krince.reminisce.shared.response.ExceptionResponseCode
import com.krince.reminisce.shared.response.SuccessResponseCode
import com.krince.reminisce.testutil.TestConfig
import com.krince.reminisce.testutil.fixture.TestChildFixture
import com.krince.reminisce.testutil.fixture.TestJwtTokenFixture
import com.krince.reminisce.testutil.fixture.TestSpeakingSessionFixture
import com.krince.reminisce.testutil.fixture.TestStoryFixture
import com.krince.reminisce.testutil.fixture.TestUserFixture
import java.time.LocalDateTime
import io.kotest.core.annotation.DisplayName
import io.kotest.core.spec.style.FunSpec
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.parsing.Parser
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.nullValue
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("localtest")
@Import(TestConfig::class)
@io.kotest.core.annotation.Tags("test", "integrationTest")
@DisplayName("StoryControllerImpl 통합테스트")
class StoryControllerImplTest(
    @param:LocalServerPort private val port: Int,
    private val testUserFixture: TestUserFixture,
    private val testStoryFixture: TestStoryFixture,
    private val testJwtTokenFixture: TestJwtTokenFixture,
    private val testChildFixture: TestChildFixture,
    private val testSpeakingSessionFixture: TestSpeakingSessionFixture,
) : FunSpec({

    fun uniqueSuffix(): String = "${System.currentTimeMillis()}-${System.nanoTime()}"

    fun userEntity(userId: String): UserOrmEntity =
        UserOrmEntity(
            userId = userId,
            email = null,
            nickname = "홍길동",
            provider = "KAKAO",
            role = "ROLE_USER",
            providerId = "kakao-$userId",
        )

    fun authorizedToken(): String {
        val guardianId = "guardian-${uniqueSuffix()}"
        testUserFixture.saveUser(userEntity(guardianId))

        return testJwtTokenFixture.generateAccessToken(guardianId)
    }

    fun storyEntity(
        storyId: String,
        status: String = StoryStatus.PUBLISHED.name,
        situation: String? = null,
        childRole: String? = null,
        postActivityConfig: PostActivityConfig? = null,
        difficulty: String = "보통",
    ): StoryOrmEntity = StoryOrmEntity(
        storyId = storyId,
        title = "제목-$storyId",
        summary = "요약-$storyId",
        intro = "도입-$storyId",
        situation = situation,
        childRole = childRole,
        difficulty = difficulty,
        estimatedMinutes = 20,
        representativeImageUrl = "/files/$storyId.png",
        status = status,
        postActivityConfig = postActivityConfig,
    )

    fun childEntity(childId: String, guardianId: String): ChildOrmEntity = ChildOrmEntity(
        childId = childId,
        guardianId = guardianId,
        nickname = "테스트아이",
        birthYear = 2018,
    )

    fun sessionEntity(sessionId: String, childId: String, storyId: String): SpeakingSessionOrmEntity =
        SpeakingSessionOrmEntity(
            sessionId = sessionId,
            childId = childId,
            storyId = storyId,
            status = "IN_PROGRESS",
            startedAt = LocalDateTime.now().minusMinutes(10),
            lastActivityAt = LocalDateTime.now().minusMinutes(1),
        )

    fun narrationEntity(storyId: String, sceneOrder: Short): SceneOrmEntity = SceneOrmEntity(
        sceneId = "sc-$sceneOrder-$storyId",
        storyId = storyId,
        sceneOrder = sceneOrder,
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

    fun dialogueEntity(storyId: String, sceneOrder: Short): SceneOrmEntity = SceneOrmEntity(
        sceneId = "sc-$sceneOrder-$storyId",
        storyId = storyId,
        sceneOrder = sceneOrder,
        sceneType = SceneType.DIALOGUE.name,
        sceneDescription = "대화 설명 $sceneOrder",
        characterName = "ch_banggui_daughter_in_law",
        characterDisplayName = "방귀쟁이 며느리",
        characterOpening = "ㅇㅇ아, 내 방귀가 너무 크다는 걸 알면 가족들이 나를 이상하게 생각하지 않을까?",
        characterClosing = "그래도 아직은 못 말하겠어. 조금만 더 참아 볼게.",
        conflict = null,
        sceneGoal = "며느리의 입장을 이해하고 공감해준다",
        requiredElements = listOf(
            ThinkingElement.PERSPECTIVE,
            ThinkingElement.EMOTION,
            ThinkingElement.REASON,
            ThinkingElement.SOLUTION,
        ),
        preferredTurns = null,
        maxTurns = 4,
    )

    fun dialogueEntityWithMission(storyId: String, sceneOrder: Short, mission: Mission): SceneOrmEntity = SceneOrmEntity(
        sceneId = "sc-$sceneOrder-$storyId",
        storyId = storyId,
        sceneOrder = sceneOrder,
        sceneType = SceneType.DIALOGUE.name,
        sceneDescription = "대화 설명 $sceneOrder",
        characterName = "ch_banggui_village_chief",
        characterDisplayName = "마을 이장",
        characterOpening = "뾰족한 방법이 없겠는가?",
        characterClosing = "고맙소!",
        conflict = null,
        sceneGoal = "해결책을 제안한다",
        requiredElements = listOf(ThinkingElement.SOLUTION),
        preferredTurns = null,
        maxTurns = 5,
        mission = mission,
    )

    fun topicEntity(storyId: String, topic: String): StoryTopicOrmEntity = StoryTopicOrmEntity(
        id = "topic-$topic-$storyId",
        storyId = storyId,
        topic = topic,
    )

    beforeSpec {
        RestAssured.port = port
        RestAssured.basePath = "/api"
        RestAssured.defaultParser = Parser.JSON
    }

    beforeTest {
        testSpeakingSessionFixture.deleteAllBatch()
        testStoryFixture.deleteAllBatch()
        testChildFixture.deleteAllBatch()
        testUserFixture.deleteAllBatch()
    }

    context("getStories") {
        context("성공") {
            test("공개 이야기의 요약 필드만 반환하고 draft는 나오지 않는다") {
                val token = authorizedToken()
                val publishedId = "published-${uniqueSuffix()}"
                val draftId = "draft-${uniqueSuffix()}"
                testStoryFixture.saveStory(storyEntity(publishedId))
                testStoryFixture.saveStory(storyEntity(draftId, status = StoryStatus.DRAFT.name))
                testStoryFixture.saveTopic(topicEntity(publishedId, "다름"))
                testStoryFixture.saveTopic(topicEntity(publishedId, "자기이해"))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .`when`()
                    .get("/stories")
                    .then()
                    .statusCode(200)
                    .body("success", equalTo(true))
                    .body("code", equalTo(200))
                    .body("message", equalTo(SuccessResponseCode.OK.message))
                    .body("data", hasSize<Any>(1))
                    .body("data[0].storyId", equalTo(publishedId))
                    .body("data[0].title", equalTo("제목-$publishedId"))
                    .body("data[0].representativeImageUrl", equalTo("/files/$publishedId.png"))
                    .body("data[0].estimatedMinutes", equalTo(20))
                    .body("data[0].topics", containsInAnyOrder("다름", "자기이해"))
            }

            test("topic 파라미터로 필터하면 그 주제를 가진 공개 이야기만 반환한다") {
                val token = authorizedToken()
                val matchedId = "matched-${uniqueSuffix()}"
                val unmatchedId = "unmatched-${uniqueSuffix()}"
                testStoryFixture.saveStory(storyEntity(matchedId))
                testStoryFixture.saveStory(storyEntity(unmatchedId))
                testStoryFixture.saveTopic(topicEntity(matchedId, "다름"))
                testStoryFixture.saveTopic(topicEntity(unmatchedId, "용기"))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .queryParam("topic", "다름")
                    .`when`()
                    .get("/stories")
                    .then()
                    .statusCode(200)
                    .body("success", equalTo(true))
                    .body("data", hasSize<Any>(1))
                    .body("data[0].storyId", equalTo(matchedId))
                    .body("data[0].topics", contains("다름"))
            }
        }
        context("예외케이스") {
            test("토큰이 없으면 401과 EMPTY_TOKEN을 반환한다") {
                RestAssured.given()
                    .contentType(ContentType.JSON)
                    .`when`()
                    .get("/stories")
                    .then()
                    .statusCode(401)
                    .body("success", equalTo(false))
                    .body("code", equalTo(401))
                    .body("detailCode", equalTo(ExceptionResponseCode.EMPTY_TOKEN.detailCode))
                    .body("message", equalTo("토큰이 없습니다."))
            }
        }
    }

    context("getStory") {
        context("성공") {
            test("상세 정보와 순서대로 정렬된 장면을 타입별 필드로 반환한다") {
                val token = authorizedToken()
                val storyId = "detail-${uniqueSuffix()}"
                val postActivityConfig = PostActivityConfig(
                    cards = listOf(
                        PostActivityConfig.Card(id = "card_1", text = "며느리가 방귀를 참았어요.", correctOrder = 1),
                        PostActivityConfig.Card(id = "card_2", text = "방귀가 크게 터졌어요.", correctOrder = 2),
                    ),
                    retellingKeywords = listOf("며느리", "방귀", "배나무"),
                )
                testStoryFixture.saveStory(
                    storyEntity(
                        storyId = storyId,
                        situation = "상황-$storyId",
                        childRole = "역할-$storyId",
                        postActivityConfig = postActivityConfig,
                    ),
                )
                testStoryFixture.saveScene(dialogueEntity(storyId, 3))
                testStoryFixture.saveScene(narrationEntity(storyId, 1))
                testStoryFixture.saveScene(narrationEntity(storyId, 2))
                testStoryFixture.saveTopic(topicEntity(storyId, "다름"))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .`when`()
                    .get("/stories/$storyId")
                    .then()
                    .statusCode(200)
                    .body("success", equalTo(true))
                    .body("code", equalTo(200))
                    .body("message", equalTo(SuccessResponseCode.OK.message))
                    .body("data.storyId", equalTo(storyId))
                    .body("data.title", equalTo("제목-$storyId"))
                    .body("data.intro", equalTo("도입-$storyId"))
                    .body("data.situation", equalTo("상황-$storyId"))
                    .body("data.childRole", equalTo("역할-$storyId"))
                    .body("data.postActivity.cards.id", contains("card_1", "card_2"))
                    .body("data.postActivity.cards.correctOrder", contains(1, 2))
                    .body("data.postActivity.retellingKeywords", contains("며느리", "방귀", "배나무"))
                    .body("data.scenes", hasSize<Any>(3))
                    .body("data.scenes.sceneOrder", contains(1, 2, 3))
                    .body("data.scenes[0].sceneType", equalTo(SceneType.NARRATION.name))
                    .body("data.scenes[0].sceneDescription", equalTo("전개 설명 1"))
                    .body("data.scenes[0].characterName", nullValue())
                    .body("data.scenes[0].requiredElements", nullValue())
                    .body("data.scenes[2].sceneType", equalTo(SceneType.DIALOGUE.name))
                    .body("data.scenes[2].characterName", equalTo("ch_banggui_daughter_in_law"))
                    .body("data.scenes[2].characterDisplayName", equalTo("방귀쟁이 며느리"))
                    .body(
                        "data.scenes[2].characterOpening",
                        equalTo("ㅇㅇ아, 내 방귀가 너무 크다는 걸 알면 가족들이 나를 이상하게 생각하지 않을까?"),
                    )
                    .body("data.scenes[2].characterClosing", equalTo("그래도 아직은 못 말하겠어. 조금만 더 참아 볼게."))
                    .body("data.scenes[2].sceneGoal", equalTo("며느리의 입장을 이해하고 공감해준다"))
                    .body(
                        "data.scenes[2].requiredElements",
                        contains("PERSPECTIVE", "EMOTION", "REASON", "SOLUTION"),
                    )
                    .body("data.scenes[2].preferredTurns", nullValue())
                    .body("data.scenes[2].maxTurns", equalTo(4))
                    .body("data.scenes[0].mission", nullValue())
                    .body("data.scenes[1].mission", nullValue())
                    .body("data.scenes[2].mission", nullValue())
            }

            test("미션이 있는 대화 장면은 mission.goal·examples를 담아 반환하고 미션 없는 장면은 mission이 null이다") {
                val token = authorizedToken()
                val storyId = "mission-${uniqueSuffix()}"
                val mission = Mission(
                    goal = "높은 배나무의 배를 떨어뜨리기 위해 며느리의 방귀를 안전하게 사용할 수 있는 방법 찾기",
                    examples = listOf("무엇을 사용할 것인지", "주변 사람들과 시아버지는 어디로 피해야 할지"),
                )
                testStoryFixture.saveStory(storyEntity(storyId))
                testStoryFixture.saveScene(narrationEntity(storyId, 1))
                testStoryFixture.saveScene(dialogueEntityWithMission(storyId, 2, mission))
                testStoryFixture.saveTopic(topicEntity(storyId, "다름"))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .`when`()
                    .get("/stories/$storyId")
                    .then()
                    .statusCode(200)
                    .body("data.scenes[0].mission", nullValue())
                    .body("data.scenes[1].mission.goal", equalTo("높은 배나무의 배를 떨어뜨리기 위해 며느리의 방귀를 안전하게 사용할 수 있는 방법 찾기"))
                    .body("data.scenes[1].mission.examples", contains("무엇을 사용할 것인지", "주변 사람들과 시아버지는 어디로 피해야 할지"))
            }
        }
        context("예외케이스") {
            test("존재하지 않는 이야기 id로 조회하면 404와 NOT_FOUND_STORY를 반환한다") {
                val token = authorizedToken()

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .`when`()
                    .get("/stories/unknown-${uniqueSuffix()}")
                    .then()
                    .statusCode(404)
                    .body("success", equalTo(false))
                    .body("code", equalTo(404))
                    .body("detailCode", equalTo(ExceptionResponseCode.NOT_FOUND_STORY.detailCode))
                    .body("message", equalTo("이야기가 존재하지 않습니다."))
            }

            test("draft 이야기 id로 조회하면 존재하지 않는 id와 같은 404 NOT_FOUND_STORY로 은닉한다") {
                val token = authorizedToken()
                val draftId = "draft-${uniqueSuffix()}"
                testStoryFixture.saveStory(storyEntity(draftId, status = StoryStatus.DRAFT.name))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .`when`()
                    .get("/stories/$draftId")
                    .then()
                    .statusCode(404)
                    .body("success", equalTo(false))
                    .body("code", equalTo(404))
                    .body("detailCode", equalTo(ExceptionResponseCode.NOT_FOUND_STORY.detailCode))
                    .body("message", equalTo("이야기가 존재하지 않습니다."))
            }

            test("토큰이 없으면 401과 EMPTY_TOKEN을 반환한다") {
                RestAssured.given()
                    .contentType(ContentType.JSON)
                    .`when`()
                    .get("/stories/any-story-id")
                    .then()
                    .statusCode(401)
                    .body("success", equalTo(false))
                    .body("code", equalTo(401))
                    .body("detailCode", equalTo(ExceptionResponseCode.EMPTY_TOKEN.detailCode))
                    .body("message", equalTo("토큰이 없습니다."))
            }
        }
    }

    context("getRecommendedStories") {
        fun authorizedTokenWithGuardian(): Pair<String, String> {
            val guardianId = "guardian-${uniqueSuffix()}"
            testUserFixture.saveUser(userEntity(guardianId))

            return Pair(testJwtTokenFixture.generateAccessToken(guardianId), guardianId)
        }

        context("성공") {
            test("게시 이야기 3건 중 아이가 시작한 1건을 제외한 2건을 난이도 오름차순으로 반환한다") {
                val (token, guardianId) = authorizedTokenWithGuardian()
                val childId = "child-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))

                val storyIdA = "story-a-${uniqueSuffix()}"
                val storyIdB = "story-b-${uniqueSuffix()}"
                val storyIdStarted = "story-started-${uniqueSuffix()}"
                testStoryFixture.saveStory(storyEntity(storyIdA, difficulty = "나"))
                testStoryFixture.saveStory(storyEntity(storyIdB, difficulty = "가"))
                testStoryFixture.saveStory(storyEntity(storyIdStarted, difficulty = "다"))
                testSpeakingSessionFixture.save(sessionEntity("session-${uniqueSuffix()}", childId, storyIdStarted))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .queryParam("childId", childId)
                    .`when`()
                    .get("/stories/recommendations")
                    .then()
                    .statusCode(200)
                    .body("success", equalTo(true))
                    .body("code", equalTo(200))
                    .body("message", equalTo(SuccessResponseCode.OK.message))
                    .body("data", hasSize<Any>(2))
                    .body("data[0].storyId", equalTo(storyIdB))
                    .body("data[1].storyId", equalTo(storyIdA))
            }

            test("미게시(DRAFT) 이야기는 추천에서 제외된다") {
                val (token, guardianId) = authorizedTokenWithGuardian()
                val childId = "child-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))

                val publishedId = "published-${uniqueSuffix()}"
                val draftId = "draft-${uniqueSuffix()}"
                testStoryFixture.saveStory(storyEntity(publishedId))
                testStoryFixture.saveStory(storyEntity(draftId, status = StoryStatus.DRAFT.name))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .queryParam("childId", childId)
                    .`when`()
                    .get("/stories/recommendations")
                    .then()
                    .statusCode(200)
                    .body("data", hasSize<Any>(1))
                    .body("data[0].storyId", equalTo(publishedId))
            }

            test("빈 추천 목록도 200 OK로 응답한다") {
                val (token, guardianId) = authorizedTokenWithGuardian()
                val childId = "child-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .queryParam("childId", childId)
                    .`when`()
                    .get("/stories/recommendations")
                    .then()
                    .statusCode(200)
                    .body("success", equalTo(true))
                    .body("data", hasSize<Any>(0))
            }
        }

        context("예외케이스") {
            test("남의 아이 childId로 요청하면 404와 NOT_FOUND를 반환한다") {
                val (token, guardianId) = authorizedTokenWithGuardian()
                val otherGuardianId = "other-guardian-${uniqueSuffix()}"
                testUserFixture.saveUser(userEntity(otherGuardianId))
                val otherChildId = "child-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(otherChildId, otherGuardianId))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .queryParam("childId", otherChildId)
                    .`when`()
                    .get("/stories/recommendations")
                    .then()
                    .statusCode(404)
                    .body("success", equalTo(false))
                    .body("code", equalTo(404))
                    .body("detailCode", equalTo(ExceptionResponseCode.NOT_FOUND.detailCode))
            }

            test("존재하지 않는 childId로 요청하면 404와 NOT_FOUND를 반환한다") {
                val (token, guardianId) = authorizedTokenWithGuardian()

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .queryParam("childId", "nonexistent-child-${uniqueSuffix()}")
                    .`when`()
                    .get("/stories/recommendations")
                    .then()
                    .statusCode(404)
                    .body("success", equalTo(false))
                    .body("code", equalTo(404))
                    .body("detailCode", equalTo(ExceptionResponseCode.NOT_FOUND.detailCode))
            }

            test("토큰이 없으면 401과 EMPTY_TOKEN을 반환한다") {
                RestAssured.given()
                    .contentType(ContentType.JSON)
                    .queryParam("childId", "any-child-id")
                    .`when`()
                    .get("/stories/recommendations")
                    .then()
                    .statusCode(401)
                    .body("success", equalTo(false))
                    .body("code", equalTo(401))
                    .body("detailCode", equalTo(ExceptionResponseCode.EMPTY_TOKEN.detailCode))
                    .body("message", equalTo("토큰이 없습니다."))
            }
        }
    }
})
