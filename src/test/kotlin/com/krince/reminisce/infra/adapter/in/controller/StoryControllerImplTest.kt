package com.krince.reminisce.infra.adapter.`in`.controller

import com.krince.reminisce.domain.model.story.CharacterVoice
import com.krince.reminisce.domain.model.story.Mission
import com.krince.reminisce.domain.model.story.VoiceAgeGroup
import com.krince.reminisce.domain.model.story.VoiceGender
import com.krince.reminisce.domain.model.story.vo.PostActivityConfig
import com.krince.reminisce.domain.model.story.vo.SceneType
import com.krince.reminisce.domain.model.story.vo.StoryGenre
import com.krince.reminisce.domain.model.story.vo.StoryStatus
import com.krince.reminisce.domain.model.story.vo.ThinkingElement
import com.krince.reminisce.infra.adapter.out.persistence.child.entity.ChildOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.savedstory.entity.SavedStoryOrmEntity
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
import com.krince.reminisce.testutil.fixture.TestSavedStoryFixture
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
    private val testSavedStoryFixture: TestSavedStoryFixture,
) : FunSpec({

    fun uniqueSuffix(): String = "${System.currentTimeMillis()}-${System.nanoTime()}"

    fun savedStoryEntity(childId: String, storyId: String): SavedStoryOrmEntity = SavedStoryOrmEntity(
        savedStoryId = "saved-$storyId-$childId",
        childId = childId,
        storyId = storyId,
    )

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
        title: String = "제목-$storyId",
        storyGenre: String? = null,
    ): StoryOrmEntity = StoryOrmEntity(
        storyId = storyId,
        title = title,
        summary = "요약-$storyId",
        intro = "도입-$storyId",
        situation = situation,
        childRole = childRole,
        difficulty = difficulty,
        estimatedMinutes = 20,
        representativeImageUrl = "/files/$storyId.png",
        status = status,
        storyGenre = storyGenre,
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

    fun sceneImageUrl(storyId: String, sceneOrder: Short): String = "/files/$storyId-scene-$sceneOrder.png"

    fun narrationEntity(storyId: String, sceneOrder: Short, chapter: Short = 1): SceneOrmEntity = SceneOrmEntity(
        sceneId = "sc-$sceneOrder-$storyId",
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
        imageUrl = sceneImageUrl(storyId, sceneOrder),
    )

    fun dialogueEntity(storyId: String, sceneOrder: Short, chapter: Short = 1): SceneOrmEntity = SceneOrmEntity(
        sceneId = "sc-$sceneOrder-$storyId",
        storyId = storyId,
        sceneOrder = sceneOrder,
        chapter = chapter,
        sceneType = SceneType.DIALOGUE.name,
        sceneDescription = "대화 설명 $sceneOrder",
        characterName = "ch_banggui_daughter_in_law",
        characterDisplayName = "방귀쟁이 며느리",
        characterOpening = null,
        characterClosing = null,
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
        characterVoice = CharacterVoice(
            gender = VoiceGender.FEMALE,
            ageGroup = VoiceAgeGroup.ADULT,
            voiceProfile = "young_woman_gentle",
        ),
        imageUrl = sceneImageUrl(storyId, sceneOrder),
        characterImageUrl = "/files/char-ch_banggui_daughter_in_law.png",
    )

    fun characterLineEntity(
        storyId: String,
        sceneOrder: Short,
        chapter: Short = 1,
        characterOpening: String = "ㅇㅇ아, 안녕? 나는 방귀쟁이 며느리야.",
    ): SceneOrmEntity = SceneOrmEntity(
        sceneId = "sc-$sceneOrder-$storyId",
        storyId = storyId,
        sceneOrder = sceneOrder,
        chapter = chapter,
        sceneType = SceneType.CHARACTER_LINE.name,
        sceneDescription = "캐릭터 대사 설명 $sceneOrder",
        characterName = "ch_banggui_daughter_in_law",
        characterDisplayName = "방귀쟁이 며느리",
        characterOpening = characterOpening,
        characterClosing = null,
        conflict = null,
        sceneGoal = null,
        requiredElements = null,
        preferredTurns = null,
        maxTurns = null,
        characterVoice = CharacterVoice(
            gender = VoiceGender.FEMALE,
            ageGroup = VoiceAgeGroup.ADULT,
            voiceProfile = "young_woman_gentle",
        ),
        imageUrl = sceneImageUrl(storyId, sceneOrder),
        characterImageUrl = "/files/char-ch_banggui_daughter_in_law.png",
    )

    fun dialogueEntityWithMission(storyId: String, sceneOrder: Short, mission: Mission): SceneOrmEntity = SceneOrmEntity(
        sceneId = "sc-$sceneOrder-$storyId",
        storyId = storyId,
        sceneOrder = sceneOrder,
        chapter = 1,
        sceneType = SceneType.DIALOGUE.name,
        sceneDescription = "대화 설명 $sceneOrder",
        characterName = "ch_banggui_village_chief",
        characterDisplayName = "마을 이장",
        characterOpening = null,
        characterClosing = null,
        conflict = null,
        sceneGoal = "해결책을 제안한다",
        requiredElements = listOf(ThinkingElement.SOLUTION),
        preferredTurns = null,
        maxTurns = 5,
        mission = mission,
        characterVoice = CharacterVoice(
            gender = VoiceGender.MALE,
            ageGroup = VoiceAgeGroup.ELDER,
            voiceProfile = "elderly_man_warm",
        ),
        characterImageUrl = "/files/char-ch_banggui_village_chief.png",
    )

    fun dialogueEntityWithCharacterVoice(
        storyId: String,
        sceneOrder: Short,
        characterName: String,
        characterDisplayName: String,
        characterVoice: CharacterVoice,
    ): SceneOrmEntity = SceneOrmEntity(
        sceneId = "sc-$sceneOrder-$storyId",
        storyId = storyId,
        sceneOrder = sceneOrder,
        chapter = 1,
        sceneType = SceneType.DIALOGUE.name,
        sceneDescription = "대화 설명 $sceneOrder",
        characterName = characterName,
        characterDisplayName = characterDisplayName,
        characterOpening = null,
        characterClosing = null,
        conflict = null,
        sceneGoal = "장면 발화 목표 $sceneOrder",
        requiredElements = listOf(ThinkingElement.PERSPECTIVE, ThinkingElement.EMOTION),
        preferredTurns = null,
        maxTurns = 4,
        characterVoice = characterVoice,
        characterImageUrl = "/files/char-$characterName.png",
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
        testSavedStoryFixture.deleteAllBatch()
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
                    .body("data[0].difficulty", equalTo("보통"))
                    .body("data[0].isBookmarked", equalTo(false))
            }

            test("childId 없이 조회하면 모든 항목 isBookmarked가 false다") {
                val token = authorizedToken()
                val publishedId = "no-child-${uniqueSuffix()}"
                testStoryFixture.saveStory(storyEntity(publishedId))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .`when`()
                    .get("/stories")
                    .then()
                    .statusCode(200)
                    .body("data", hasSize<Any>(1))
                    .body("data[0].storyId", equalTo(publishedId))
                    .body("data[0].isBookmarked", equalTo(false))
            }

            test("childId를 주면 그 아이가 찜한 이야기만 isBookmarked=true로 응답한다") {
                val guardianId = "guardian-${uniqueSuffix()}"
                testUserFixture.saveUser(userEntity(guardianId))
                val token = testJwtTokenFixture.generateAccessToken(guardianId)
                val childId = "child-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))

                val bookmarkedId = "bookmarked-${uniqueSuffix()}"
                val plainId = "plain-${uniqueSuffix()}"
                testStoryFixture.saveStory(storyEntity(bookmarkedId, difficulty = "가"))
                testStoryFixture.saveStory(storyEntity(plainId, difficulty = "나"))
                testSavedStoryFixture.save(savedStoryEntity(childId, bookmarkedId))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .queryParam("childId", childId)
                    .queryParam("sort", "DIFFICULTY")
                    .`when`()
                    .get("/stories")
                    .then()
                    .statusCode(200)
                    .body("data", hasSize<Any>(2))
                    .body("data[0].storyId", equalTo(bookmarkedId))
                    .body("data[0].isBookmarked", equalTo(true))
                    .body("data[1].storyId", equalTo(plainId))
                    .body("data[1].isBookmarked", equalTo(false))
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

            test("topic 파라미터가 빈 값이면 그 빈 주제를 가진 이야기가 없어 빈 목록을 반환한다") {
                val token = authorizedToken()
                val publishedId = "empty-topic-${uniqueSuffix()}"
                testStoryFixture.saveStory(storyEntity(publishedId))
                testStoryFixture.saveTopic(topicEntity(publishedId, "다름"))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .queryParam("topic", "")
                    .`when`()
                    .get("/stories")
                    .then()
                    .statusCode(200)
                    .body("success", equalTo(true))
                    .body("data", hasSize<Any>(0))
            }

            test("genre로 필터하면 그 장르 공개 이야기만 반환하고 장르 라벨을 노출한다") {
                val token = authorizedToken()
                val folktaleId = "folktale-${uniqueSuffix()}"
                val creativeId = "creative-${uniqueSuffix()}"
                val genrelessId = "genreless-${uniqueSuffix()}"
                testStoryFixture.saveStory(storyEntity(folktaleId, storyGenre = StoryGenre.FOLKTALE.name))
                testStoryFixture.saveStory(storyEntity(creativeId, storyGenre = StoryGenre.CREATIVE.name))
                testStoryFixture.saveStory(storyEntity(genrelessId))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .queryParam("genre", StoryGenre.FOLKTALE.name)
                    .`when`()
                    .get("/stories")
                    .then()
                    .statusCode(200)
                    .body("success", equalTo(true))
                    .body("data", hasSize<Any>(1))
                    .body("data[0].storyId", equalTo(folktaleId))
                    .body("data[0].genre", equalTo("전래동화"))
            }

            test("장르가 없는 이야기는 genre가 null로 응답된다") {
                val token = authorizedToken()
                val genrelessId = "genreless-${uniqueSuffix()}"
                testStoryFixture.saveStory(storyEntity(genrelessId))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .`when`()
                    .get("/stories")
                    .then()
                    .statusCode(200)
                    .body("data", hasSize<Any>(1))
                    .body("data[0].storyId", equalTo(genrelessId))
                    .body("data[0].genre", nullValue())
            }

            test("q로 제목 일부를 검색하면 대소문자 무시로 매칭되는 공개 이야기만 반환한다") {
                val token = authorizedToken()
                val matchedId = "qmatch-${uniqueSuffix()}"
                val unmatchedId = "qmiss-${uniqueSuffix()}"
                testStoryFixture.saveStory(storyEntity(matchedId, title = "Banggui 며느리 이야기"))
                testStoryFixture.saveStory(storyEntity(unmatchedId, title = "다른 이야기"))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .queryParam("q", "banggui")
                    .`when`()
                    .get("/stories")
                    .then()
                    .statusCode(200)
                    .body("data", hasSize<Any>(1))
                    .body("data[0].storyId", equalTo(matchedId))
            }

            test("sort=DIFFICULTY로 조회하면 난이도 오름차순으로 정렬된다") {
                val token = authorizedToken()
                val hardId = "hard-${uniqueSuffix()}"
                val easyId = "easy-${uniqueSuffix()}"
                val midId = "mid-${uniqueSuffix()}"
                testStoryFixture.saveStory(storyEntity(hardId, difficulty = "다"))
                testStoryFixture.saveStory(storyEntity(easyId, difficulty = "가"))
                testStoryFixture.saveStory(storyEntity(midId, difficulty = "나"))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .queryParam("sort", "DIFFICULTY")
                    .`when`()
                    .get("/stories")
                    .then()
                    .statusCode(200)
                    .body("data", hasSize<Any>(3))
                    .body("data.storyId", contains(easyId, midId, hardId))
            }

            test("sort=LATEST로 조회하면 최신 생성 순으로 정렬된다") {
                val token = authorizedToken()
                val firstId = "first-${uniqueSuffix()}"
                val secondId = "second-${uniqueSuffix()}"
                testStoryFixture.saveStory(storyEntity(firstId))
                Thread.sleep(10)
                testStoryFixture.saveStory(storyEntity(secondId))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .queryParam("sort", "LATEST")
                    .`when`()
                    .get("/stories")
                    .then()
                    .statusCode(200)
                    .body("data", hasSize<Any>(2))
                    .body("data.storyId", contains(secondId, firstId))
            }

            test("sort=RECOMMENDED로 조회하면 생성 오름차순(기본순)으로 정렬된다") {
                val token = authorizedToken()
                val firstId = "rec-first-${uniqueSuffix()}"
                val secondId = "rec-second-${uniqueSuffix()}"
                testStoryFixture.saveStory(storyEntity(firstId))
                Thread.sleep(10)
                testStoryFixture.saveStory(storyEntity(secondId))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .queryParam("sort", "RECOMMENDED")
                    .`when`()
                    .get("/stories")
                    .then()
                    .statusCode(200)
                    .body("data", hasSize<Any>(2))
                    .body("data.storyId", contains(firstId, secondId))
            }

            test("sort 미지정이면 RECOMMENDED(생성 오름차순) 순서로 정렬된다") {
                val token = authorizedToken()
                val firstId = "default-first-${uniqueSuffix()}"
                val secondId = "default-second-${uniqueSuffix()}"
                testStoryFixture.saveStory(storyEntity(firstId))
                Thread.sleep(10)
                testStoryFixture.saveStory(storyEntity(secondId))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .`when`()
                    .get("/stories")
                    .then()
                    .statusCode(200)
                    .body("data", hasSize<Any>(2))
                    .body("data.storyId", contains(firstId, secondId))
            }

            test("sort=POPULAR로 조회하면 세션 시작 수가 많은 이야기부터 정렬되고 세션 없는 이야기는 뒤로 간다") {
                val guardianId = "guardian-${uniqueSuffix()}"
                testUserFixture.saveUser(userEntity(guardianId))
                val token = testJwtTokenFixture.generateAccessToken(guardianId)
                val childId = "child-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))

                val twoSessionsId = "popular-two-${uniqueSuffix()}"
                val oneSessionId = "popular-one-${uniqueSuffix()}"
                val noSessionId = "popular-none-${uniqueSuffix()}"
                testStoryFixture.saveStory(storyEntity(twoSessionsId))
                testStoryFixture.saveStory(storyEntity(oneSessionId))
                testStoryFixture.saveStory(storyEntity(noSessionId))
                testSpeakingSessionFixture.save(sessionEntity("session-${uniqueSuffix()}", childId, twoSessionsId))
                testSpeakingSessionFixture.save(sessionEntity("session-${uniqueSuffix()}", childId, twoSessionsId))
                testSpeakingSessionFixture.save(sessionEntity("session-${uniqueSuffix()}", childId, oneSessionId))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .queryParam("sort", "POPULAR")
                    .`when`()
                    .get("/stories")
                    .then()
                    .statusCode(200)
                    .body("data", hasSize<Any>(3))
                    .body("data.storyId", contains(twoSessionsId, oneSessionId, noSessionId))
            }

            test("sort=POPULAR에서 세션 수가 동률이면 createdDate 최신순으로 tie-break 된다") {
                val guardianId = "guardian-${uniqueSuffix()}"
                testUserFixture.saveUser(userEntity(guardianId))
                val token = testJwtTokenFixture.generateAccessToken(guardianId)
                val childId = "child-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))

                val olderTiedId = "tie-older-${uniqueSuffix()}"
                val newerTiedId = "tie-newer-${uniqueSuffix()}"
                val noSessionId = "tie-none-${uniqueSuffix()}"
                testStoryFixture.saveStory(storyEntity(olderTiedId))
                Thread.sleep(10)
                testStoryFixture.saveStory(storyEntity(newerTiedId))
                Thread.sleep(10)
                testStoryFixture.saveStory(storyEntity(noSessionId))
                testSpeakingSessionFixture.save(sessionEntity("session-${uniqueSuffix()}", childId, olderTiedId))
                testSpeakingSessionFixture.save(sessionEntity("session-${uniqueSuffix()}", childId, newerTiedId))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .queryParam("sort", "POPULAR")
                    .`when`()
                    .get("/stories")
                    .then()
                    .statusCode(200)
                    .body("data", hasSize<Any>(3))
                    .body("data.storyId", contains(newerTiedId, olderTiedId, noSessionId))
            }

            test("genre·q·topic을 함께 주면 모두 만족하는 공개 이야기만 반환한다") {
                val token = authorizedToken()
                val targetId = "combo-target-${uniqueSuffix()}"
                val wrongGenreId = "combo-genre-${uniqueSuffix()}"
                val wrongTopicId = "combo-topic-${uniqueSuffix()}"
                testStoryFixture.saveStory(
                    storyEntity(targetId, title = "용감한 며느리", storyGenre = StoryGenre.FOLKTALE.name),
                )
                testStoryFixture.saveStory(
                    storyEntity(wrongGenreId, title = "용감한 며느리", storyGenre = StoryGenre.CREATIVE.name),
                )
                testStoryFixture.saveStory(
                    storyEntity(wrongTopicId, title = "용감한 며느리", storyGenre = StoryGenre.FOLKTALE.name),
                )
                testStoryFixture.saveTopic(topicEntity(targetId, "용기"))
                testStoryFixture.saveTopic(topicEntity(wrongGenreId, "용기"))
                testStoryFixture.saveTopic(topicEntity(wrongTopicId, "다름"))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .queryParam("genre", StoryGenre.FOLKTALE.name)
                    .queryParam("q", "며느리")
                    .queryParam("topic", "용기")
                    .`when`()
                    .get("/stories")
                    .then()
                    .statusCode(200)
                    .body("data", hasSize<Any>(1))
                    .body("data[0].storyId", equalTo(targetId))
            }
        }
        context("예외케이스") {
            test("남의 아이 childId로 조회하면 404와 NOT_FOUND를 반환한다") {
                val token = authorizedToken()
                val otherGuardianId = "other-guardian-${uniqueSuffix()}"
                testUserFixture.saveUser(userEntity(otherGuardianId))
                val otherChildId = "child-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(otherChildId, otherGuardianId))
                testStoryFixture.saveStory(storyEntity("story-${uniqueSuffix()}"))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .queryParam("childId", otherChildId)
                    .`when`()
                    .get("/stories")
                    .then()
                    .statusCode(404)
                    .body("success", equalTo(false))
                    .body("code", equalTo(404))
                    .body("detailCode", equalTo(ExceptionResponseCode.NOT_FOUND.detailCode))
            }

            test("존재하지 않는 childId로 조회하면 404와 NOT_FOUND를 반환한다") {
                val token = authorizedToken()
                testStoryFixture.saveStory(storyEntity("story-${uniqueSuffix()}"))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .queryParam("childId", "nonexistent-child-${uniqueSuffix()}")
                    .`when`()
                    .get("/stories")
                    .then()
                    .statusCode(404)
                    .body("success", equalTo(false))
                    .body("code", equalTo(404))
                    .body("detailCode", equalTo(ExceptionResponseCode.NOT_FOUND.detailCode))
            }

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
                        PostActivityConfig.Card(id = "card_1", text = "며느리가 방귀를 참았어요.", correctOrder = 1, imageUrl = "/files/banggui-card-1.png"),
                        PostActivityConfig.Card(id = "card_2", text = "방귀가 크게 터졌어요.", correctOrder = 2, imageUrl = "/files/banggui-card-2.png"),
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
                testStoryFixture.saveScene(dialogueEntity(storyId, 3, chapter = 2))
                testStoryFixture.saveScene(narrationEntity(storyId, 1, chapter = 1))
                testStoryFixture.saveScene(narrationEntity(storyId, 2, chapter = 1))
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
                    .body("data.difficulty", equalTo("보통"))
                    .body("data.topics", contains("다름"))
                    .body("data.postActivity.cards.id", contains("card_1", "card_2"))
                    .body("data.postActivity.cards.correctOrder", contains(1, 2))
                    .body("data.postActivity.cards.imageUrl", contains("/files/banggui-card-1.png", "/files/banggui-card-2.png"))
                    .body("data.postActivity.retellingKeywords", contains("며느리", "방귀", "배나무"))
                    .body("data.scenes", hasSize<Any>(3))
                    .body("data.scenes.sceneOrder", contains(1, 2, 3))
                    .body("data.scenes.chapter", contains(1, 1, 2))
                    .body("data.scenes[0].chapter", equalTo(1))
                    .body("data.scenes[2].chapter", equalTo(2))
                    .body("data.scenes[0].sceneType", equalTo(SceneType.NARRATION.name))
                    .body("data.scenes[0].sceneDescription", equalTo("전개 설명 1"))
                    .body("data.scenes[0].characterName", nullValue())
                    .body("data.scenes[0].requiredElements", nullValue())
                    .body("data.scenes[0].imageUrl", equalTo("/files/$storyId-scene-1.png"))
                    .body("data.scenes[0].characterImageUrl", nullValue())
                    .body("data.scenes[2].imageUrl", equalTo("/files/$storyId-scene-3.png"))
                    .body("data.scenes[2].characterImageUrl", equalTo("/files/char-ch_banggui_daughter_in_law.png"))
                    .body("data.scenes[2].sceneType", equalTo(SceneType.DIALOGUE.name))
                    .body("data.scenes[2].characterName", equalTo("ch_banggui_daughter_in_law"))
                    .body("data.scenes[2].characterDisplayName", equalTo("방귀쟁이 며느리"))
                    .body("data.scenes[2].characterOpening", nullValue())
                    .body("data.scenes[2].characterClosing", nullValue())
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

            test("한 챕터에 여러 CHARACTER_LINE이 화자 단위로 노출되고 DIALOGUE는 고정 대사 없이 반환된다") {
                val token = authorizedToken()
                val storyId = "character-line-${uniqueSuffix()}"
                testStoryFixture.saveStory(storyEntity(storyId))
                testStoryFixture.saveScene(narrationEntity(storyId, 1))
                testStoryFixture.saveScene(characterLineEntity(storyId, 2))
                testStoryFixture.saveScene(dialogueEntity(storyId, 3))
                testStoryFixture.saveScene(
                    characterLineEntity(storyId, 4, characterOpening = "그래, 이야기해 줘서 고마워."),
                )
                testStoryFixture.saveTopic(topicEntity(storyId, "다름"))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .`when`()
                    .get("/stories/$storyId")
                    .then()
                    .statusCode(200)
                    .body("data.scenes", hasSize<Any>(4))
                    .body("data.scenes.sceneType", contains(
                        SceneType.NARRATION.name,
                        SceneType.CHARACTER_LINE.name,
                        SceneType.DIALOGUE.name,
                        SceneType.CHARACTER_LINE.name,
                    ))
                    .body("data.scenes.chapter", contains(1, 1, 1, 1))
                    .body("data.scenes[1].sceneType", equalTo(SceneType.CHARACTER_LINE.name))
                    .body("data.scenes[1].characterName", equalTo("ch_banggui_daughter_in_law"))
                    .body("data.scenes[1].characterDisplayName", equalTo("방귀쟁이 며느리"))
                    .body("data.scenes[1].characterOpening", equalTo("ㅇㅇ아, 안녕? 나는 방귀쟁이 며느리야."))
                    .body("data.scenes[1].characterClosing", nullValue())
                    .body("data.scenes[1].characterImageUrl", equalTo("/files/char-ch_banggui_daughter_in_law.png"))
                    .body("data.scenes[1].characterVoice.voiceProfile", equalTo("young_woman_gentle"))
                    .body("data.scenes[1].sceneGoal", nullValue())
                    .body("data.scenes[1].requiredElements", nullValue())
                    .body("data.scenes[1].maxTurns", nullValue())
                    .body("data.scenes[1].mission", nullValue())
                    .body("data.scenes[2].sceneType", equalTo(SceneType.DIALOGUE.name))
                    .body("data.scenes[2].characterOpening", nullValue())
                    .body("data.scenes[2].characterClosing", nullValue())
                    .body("data.scenes[2].sceneGoal", equalTo("며느리의 입장을 이해하고 공감해준다"))
                    .body("data.scenes[2].maxTurns", equalTo(4))
                    .body("data.scenes[3].characterOpening", equalTo("그래, 이야기해 줘서 고마워."))
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

            test("음성 메타가 있는 대화 장면은 characterVoice를 담아 반환하고 내레이션은 characterVoice가 null이다") {
                val token = authorizedToken()
                val storyId = "voice-${uniqueSuffix()}"
                val daughterVoice = CharacterVoice(
                    gender = VoiceGender.FEMALE,
                    ageGroup = VoiceAgeGroup.ADULT,
                    voiceProfile = "young_woman_gentle",
                )
                val chiefVoice = CharacterVoice(
                    gender = VoiceGender.MALE,
                    ageGroup = VoiceAgeGroup.ELDER,
                    voiceProfile = "elderly_man_warm",
                )
                testStoryFixture.saveStory(storyEntity(storyId))
                testStoryFixture.saveScene(narrationEntity(storyId, 1))
                testStoryFixture.saveScene(
                    dialogueEntityWithCharacterVoice(
                        storyId = storyId,
                        sceneOrder = 2,
                        characterName = "ch_banggui_daughter_in_law",
                        characterDisplayName = "방귀쟁이 며느리",
                        characterVoice = daughterVoice,
                    ),
                )
                testStoryFixture.saveScene(
                    dialogueEntityWithCharacterVoice(
                        storyId = storyId,
                        sceneOrder = 3,
                        characterName = "ch_banggui_daughter_in_law",
                        characterDisplayName = "방귀쟁이 며느리",
                        characterVoice = daughterVoice,
                    ),
                )
                testStoryFixture.saveScene(
                    dialogueEntityWithCharacterVoice(
                        storyId = storyId,
                        sceneOrder = 4,
                        characterName = "ch_banggui_village_chief",
                        characterDisplayName = "마을 이장",
                        characterVoice = chiefVoice,
                    ),
                )
                testStoryFixture.saveTopic(topicEntity(storyId, "다름"))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .`when`()
                    .get("/stories/$storyId")
                    .then()
                    .statusCode(200)
                    .body("data.scenes[0].characterVoice", nullValue())
                    .body("data.scenes[1].characterVoice.gender", equalTo("FEMALE"))
                    .body("data.scenes[1].characterVoice.ageGroup", equalTo("ADULT"))
                    .body("data.scenes[1].characterVoice.voiceProfile", equalTo("young_woman_gentle"))
                    .body("data.scenes[2].characterVoice.voiceProfile", equalTo("young_woman_gentle"))
                    .body("data.scenes[3].characterVoice.gender", equalTo("MALE"))
                    .body("data.scenes[3].characterVoice.ageGroup", equalTo("ELDER"))
                    .body("data.scenes[3].characterVoice.voiceProfile", equalTo("elderly_man_warm"))
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
