package com.krince.reminisce.infra.adapter.`in`.controller

import com.krince.reminisce.domain.model.speakingsession.vo.SceneEndReason
import com.krince.reminisce.domain.model.speakingsession.vo.SessionStatus
import com.krince.reminisce.domain.model.story.CharacterVoice
import com.krince.reminisce.domain.model.story.Mission
import com.krince.reminisce.domain.model.story.VoiceAgeGroup
import com.krince.reminisce.domain.model.story.VoiceGender
import com.krince.reminisce.domain.model.story.vo.MissionType
import com.krince.reminisce.domain.model.story.vo.PostActivityConfig
import com.krince.reminisce.domain.model.story.vo.SceneType
import com.krince.reminisce.domain.model.story.vo.WordCard
import com.krince.reminisce.domain.model.story.vo.StoryStatus
import com.krince.reminisce.domain.model.story.vo.ThinkingElement
import com.krince.reminisce.domain.model.utteranceanalysis.DetectedElement
import com.krince.reminisce.domain.model.utteranceanalysis.vo.ChildIntent
import com.krince.reminisce.domain.model.utteranceanalysis.vo.UtteranceValidity
import com.krince.reminisce.infra.adapter.out.persistence.child.entity.ChildOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.childconsent.entity.ChildConsentOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.message.entity.MessageOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.postactivityresult.entity.PostActivityResultOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.speakingsession.entity.SpeakingSessionOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.story.entity.SceneOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.story.entity.StoryOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.story.entity.StoryTopicOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.user.entity.UserOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.utteranceanalysis.entity.UtteranceAnalysisOrmEntity
import com.krince.reminisce.shared.response.ExceptionResponseCode
import com.krince.reminisce.shared.response.SuccessResponseCode
import com.krince.reminisce.testutil.TestConfig
import com.krince.reminisce.testutil.fixture.TestChildConsentFixture
import com.krince.reminisce.testutil.fixture.TestChildFixture
import com.krince.reminisce.testutil.fixture.TestJwtTokenFixture
import com.krince.reminisce.testutil.fixture.TestMessageFixture
import com.krince.reminisce.testutil.fixture.TestMissionResultFixture
import com.krince.reminisce.testutil.fixture.TestPostActivityResultFixture
import com.krince.reminisce.testutil.fixture.TestReportFixture
import com.krince.reminisce.testutil.fixture.TestSpeakingSessionFixture
import com.krince.reminisce.testutil.fixture.TestStoryFixture
import com.krince.reminisce.testutil.fixture.TestUserFixture
import com.krince.reminisce.testutil.fixture.TestUtteranceAnalysisFixture
import io.kotest.core.annotation.DisplayName
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.restassured.RestAssured
import io.restassured.builder.MultiPartSpecBuilder
import io.restassured.http.ContentType
import io.restassured.parsing.Parser
import io.restassured.specification.MultiPartSpecification
import java.nio.charset.StandardCharsets
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.emptyOrNullString
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.nullValue
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("localtest")
@Import(TestConfig::class)
@io.kotest.core.annotation.Tags("test", "integrationTest")
@DisplayName("SpeakingSessionControllerImpl 통합테스트")
class SpeakingSessionControllerImplTest(
    @param:LocalServerPort private val port: Int,
    private val testUserFixture: TestUserFixture,
    private val testChildFixture: TestChildFixture,
    private val testChildConsentFixture: TestChildConsentFixture,
    private val testStoryFixture: TestStoryFixture,
    private val testSpeakingSessionFixture: TestSpeakingSessionFixture,
    private val testMessageFixture: TestMessageFixture,
    private val testUtteranceAnalysisFixture: TestUtteranceAnalysisFixture,
    private val testPostActivityResultFixture: TestPostActivityResultFixture,
    private val testMissionResultFixture: TestMissionResultFixture,
    private val testReportFixture: TestReportFixture,
    private val testJwtTokenFixture: TestJwtTokenFixture,
) : FunSpec({

    val consentVersion = "v1.0"
    val authenticatedParent = "AUTHENTICATED_PARENT"
    val defaultBirthYear: Short = 2019

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

    fun childEntity(childId: String, guardianId: String): ChildOrmEntity =
        ChildOrmEntity(childId = childId, guardianId = guardianId, nickname = "토토", birthYear = defaultBirthYear)

    fun consentEntity(childId: String, withdrawnAt: LocalDateTime? = null): ChildConsentOrmEntity =
        ChildConsentOrmEntity(
            consentId = "consent-$childId",
            childId = childId,
            consentVersion = consentVersion,
            verificationMethod = authenticatedParent,
            consentedAt = LocalDateTime.now().minusDays(1),
            withdrawnAt = withdrawnAt,
        )

    fun storyEntity(
        storyId: String,
        status: String = StoryStatus.PUBLISHED.name,
        postActivityConfig: PostActivityConfig? = null,
    ): StoryOrmEntity = StoryOrmEntity(
        storyId = storyId,
        title = "제목-$storyId",
        summary = "요약-$storyId",
        intro = "도입-$storyId",
        situation = null,
        childRole = null,
        difficulty = "보통",
        estimatedMinutes = 20,
        representativeImageUrl = "/files/$storyId.png",
        status = status,
        postActivityConfig = postActivityConfig,
    )

    fun authorizedGuardian(): Pair<String, String> {
        val guardianId = "guardian-${uniqueSuffix()}"
        testUserFixture.saveUser(userEntity(guardianId))

        return guardianId to testJwtTokenFixture.generateAccessToken(guardianId)
    }

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
        requiredElements = listOf(ThinkingElement.PERSPECTIVE, ThinkingElement.EMOTION),
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

    fun dialogueEntityWithMission(storyId: String, sceneOrder: Short, mission: Mission): SceneOrmEntity = SceneOrmEntity(
        sceneId = "sc-$sceneOrder-$storyId",
        storyId = storyId,
        sceneOrder = sceneOrder,
        chapter = 1,
        sceneType = SceneType.DIALOGUE.name,
        sceneDescription = "대화 설명 $sceneOrder",
        characterName = "ch_banggui_daughter_in_law",
        characterDisplayName = "방귀쟁이 며느리",
        characterOpening = null,
        characterClosing = null,
        conflict = null,
        sceneGoal = "다름을 긍정적으로 받아들인다",
        requiredElements = listOf(ThinkingElement.EMOTION, ThinkingElement.PERSPECTIVE),
        preferredTurns = null,
        maxTurns = 4,
        mission = mission,
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
        characterOpening: String = "ㅇㅇ아, 내 이야기를 들어 줄래?",
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

    fun sessionEntity(
        sessionId: String,
        childId: String,
        storyId: String,
        currentSceneId: String? = null,
        accumulatedElements: List<ThinkingElement> = emptyList(),
        currentChildTurnCount: Int = 0,
        turnsWithoutNewElement: Int = 0,
        sceneEndReason: String? = null,
        status: SessionStatus = SessionStatus.IN_PROGRESS,
    ): SpeakingSessionOrmEntity = SpeakingSessionOrmEntity(
        sessionId = sessionId,
        childId = childId,
        storyId = storyId,
        currentSceneId = currentSceneId,
        status = status.name,
        startedAt = LocalDateTime.now().minusMinutes(5),
        lastActivityAt = LocalDateTime.now().minusMinutes(1),
        accumulatedElements = accumulatedElements,
        currentChildTurnCount = currentChildTurnCount,
        turnsWithoutNewElement = turnsWithoutNewElement,
        sceneEndReason = sceneEndReason,
    )

    beforeSpec {
        RestAssured.port = port
        RestAssured.basePath = "/api"
        RestAssured.defaultParser = Parser.JSON
    }

    beforeTest {
        testReportFixture.deleteAllBatch()
        testUtteranceAnalysisFixture.deleteAllBatch()
        testMessageFixture.deleteAllBatch()
        testMissionResultFixture.deleteAllBatch()
        testPostActivityResultFixture.deleteAllBatch()
        testSpeakingSessionFixture.deleteAllBatch()
        testChildConsentFixture.deleteAllBatch()
        testChildFixture.deleteAllBatch()
        testStoryFixture.deleteAllBatch()
        testUserFixture.deleteAllBatch()
    }

    context("startSpeakingSession") {
        context("성공") {
            test("동의 있는 내 아이와 공개 이야기로 시작하면 201과 in_progress 세션을 반환하고 1건 저장된다") {
                val (guardianId, token) = authorizedGuardian()
                val childId = "child-${uniqueSuffix()}"
                val storyId = "story-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))
                testChildConsentFixture.saveConsent(consentEntity(childId))
                testStoryFixture.saveStory(storyEntity(storyId))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .body(mapOf("childId" to childId, "storyId" to storyId))
                    .`when`()
                    .post("/speaking-sessions")
                    .then()
                    .statusCode(201)
                    .body("success", equalTo(true))
                    .body("code", equalTo(201))
                    .body("message", equalTo(SuccessResponseCode.CREATED.message))
                    .body("data.childId", equalTo(childId))
                    .body("data.storyId", equalTo(storyId))
                    .body("data.status", equalTo("IN_PROGRESS"))
                    .body("data.currentSceneId", nullValue())

                testSpeakingSessionFixture.findAllByChildIdAndStoryId(childId, storyId).size shouldBe 1
            }

            test("이미 진행 중인 세션이 있으면 재요청 시 200과 같은 sessionId를 반환하고 여전히 1건이다") {
                val (guardianId, token) = authorizedGuardian()
                val childId = "child-${uniqueSuffix()}"
                val storyId = "story-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))
                testChildConsentFixture.saveConsent(consentEntity(childId))
                testStoryFixture.saveStory(storyEntity(storyId))

                val firstSessionId = RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .body(mapOf("childId" to childId, "storyId" to storyId))
                    .`when`()
                    .post("/speaking-sessions")
                    .then()
                    .statusCode(201)
                    .extract()
                    .path<String>("data.sessionId")

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .body(mapOf("childId" to childId, "storyId" to storyId))
                    .`when`()
                    .post("/speaking-sessions")
                    .then()
                    .statusCode(200)
                    .body("success", equalTo(true))
                    .body("code", equalTo(200))
                    .body("message", equalTo(SuccessResponseCode.OK.message))
                    .body("data.sessionId", equalTo(firstSessionId))
                    .body("data.status", equalTo("IN_PROGRESS"))

                testSpeakingSessionFixture.findAllByChildIdAndStoryId(childId, storyId).size shouldBe 1
            }
        }

        context("예외케이스") {
            test("동의가 없으면 422와 CONSENT_REQUIRED를 반환하고 세션은 저장되지 않는다") {
                val (guardianId, token) = authorizedGuardian()
                val childId = "child-${uniqueSuffix()}"
                val storyId = "story-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))
                testStoryFixture.saveStory(storyEntity(storyId))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .body(mapOf("childId" to childId, "storyId" to storyId))
                    .`when`()
                    .post("/speaking-sessions")
                    .then()
                    .statusCode(422)
                    .body("success", equalTo(false))
                    .body("code", equalTo(422))
                    .body("detailCode", equalTo(ExceptionResponseCode.CONSENT_REQUIRED.detailCode))
                    .body("message", equalTo("법정대리인 동의가 없어 세션을 시작할 수 없습니다."))

                testSpeakingSessionFixture.count() shouldBe 0L
            }

            test("동의가 철회되었으면 422와 CONSENT_REQUIRED를 반환하고 세션은 저장되지 않는다") {
                val (guardianId, token) = authorizedGuardian()
                val childId = "child-${uniqueSuffix()}"
                val storyId = "story-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))
                testChildConsentFixture.saveConsent(consentEntity(childId, withdrawnAt = LocalDateTime.now()))
                testStoryFixture.saveStory(storyEntity(storyId))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .body(mapOf("childId" to childId, "storyId" to storyId))
                    .`when`()
                    .post("/speaking-sessions")
                    .then()
                    .statusCode(422)
                    .body("detailCode", equalTo(ExceptionResponseCode.CONSENT_REQUIRED.detailCode))

                testSpeakingSessionFixture.count() shouldBe 0L
            }

            test("타 보호자의 아이로 시작하면 404와 NOT_FOUND_CHILD로 은닉하고 저장되지 않는다") {
                val (_, token) = authorizedGuardian()
                val otherGuardianId = "other-${uniqueSuffix()}"
                testUserFixture.saveUser(userEntity(otherGuardianId))
                val otherChildId = "child-${uniqueSuffix()}"
                val storyId = "story-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(otherChildId, otherGuardianId))
                testChildConsentFixture.saveConsent(consentEntity(otherChildId))
                testStoryFixture.saveStory(storyEntity(storyId))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .body(mapOf("childId" to otherChildId, "storyId" to storyId))
                    .`when`()
                    .post("/speaking-sessions")
                    .then()
                    .statusCode(404)
                    .body("detailCode", equalTo(ExceptionResponseCode.NOT_FOUND_CHILD.detailCode))
                    .body("message", equalTo("아이가 존재하지 않습니다."))

                testSpeakingSessionFixture.count() shouldBe 0L
            }

            test("존재하지 않는 아이로 시작하면 404와 NOT_FOUND_CHILD를 반환한다") {
                val (_, token) = authorizedGuardian()
                val storyId = "story-${uniqueSuffix()}"
                testStoryFixture.saveStory(storyEntity(storyId))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .body(mapOf("childId" to "missing-${uniqueSuffix()}", "storyId" to storyId))
                    .`when`()
                    .post("/speaking-sessions")
                    .then()
                    .statusCode(404)
                    .body("detailCode", equalTo(ExceptionResponseCode.NOT_FOUND_CHILD.detailCode))

                testSpeakingSessionFixture.count() shouldBe 0L
            }

            test("미공개(draft) 이야기로 시작하면 404와 NOT_FOUND_STORY를 반환하고 저장되지 않는다") {
                val (guardianId, token) = authorizedGuardian()
                val childId = "child-${uniqueSuffix()}"
                val draftStoryId = "story-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))
                testChildConsentFixture.saveConsent(consentEntity(childId))
                testStoryFixture.saveStory(storyEntity(draftStoryId, status = StoryStatus.DRAFT.name))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .body(mapOf("childId" to childId, "storyId" to draftStoryId))
                    .`when`()
                    .post("/speaking-sessions")
                    .then()
                    .statusCode(404)
                    .body("detailCode", equalTo(ExceptionResponseCode.NOT_FOUND_STORY.detailCode))
                    .body("message", equalTo("이야기가 존재하지 않습니다."))

                testSpeakingSessionFixture.count() shouldBe 0L
            }

            test("존재하지 않는 이야기로 시작하면 404와 NOT_FOUND_STORY를 반환한다") {
                val (guardianId, token) = authorizedGuardian()
                val childId = "child-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))
                testChildConsentFixture.saveConsent(consentEntity(childId))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .body(mapOf("childId" to childId, "storyId" to "missing-${uniqueSuffix()}"))
                    .`when`()
                    .post("/speaking-sessions")
                    .then()
                    .statusCode(404)
                    .body("detailCode", equalTo(ExceptionResponseCode.NOT_FOUND_STORY.detailCode))

                testSpeakingSessionFixture.count() shouldBe 0L
            }

            test("토큰이 없으면 401과 EMPTY_TOKEN을 반환한다") {
                RestAssured.given()
                    .contentType(ContentType.JSON)
                    .body(mapOf("childId" to "any-child", "storyId" to "any-story"))
                    .`when`()
                    .post("/speaking-sessions")
                    .then()
                    .statusCode(401)
                    .body("success", equalTo(false))
                    .body("code", equalTo(401))
                    .body("detailCode", equalTo(ExceptionResponseCode.EMPTY_TOKEN.detailCode))
                    .body("message", equalTo("토큰이 없습니다."))
            }
        }
    }

    context("getSpeakingSessionView 와 advanceSpeakingScene") {
        context("성공") {
            test("도입 상태 세션 GET은 200과 viewType=INTRO, intro 존재, scene null을 반환한다") {
                val (guardianId, token) = authorizedGuardian()
                val childId = "child-${uniqueSuffix()}"
                val storyId = "story-${uniqueSuffix()}"
                val sessionId = "session-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))
                testStoryFixture.saveStory(storyEntity(storyId))
                testStoryFixture.saveScene(narrationEntity(storyId, 1))
                testStoryFixture.saveScene(dialogueEntity(storyId, 3))
                testSpeakingSessionFixture.save(sessionEntity(sessionId, childId, storyId, currentSceneId = null))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .`when`()
                    .get("/speaking-sessions/$sessionId")
                    .then()
                    .statusCode(200)
                    .body("success", equalTo(true))
                    .body("code", equalTo(200))
                    .body("message", equalTo(SuccessResponseCode.OK.message))
                    .body("data.viewType", equalTo("INTRO"))
                    .body("data.intro", equalTo("도입-$storyId"))
                    .body("data.scene", nullValue())
            }

            test("도입 상태 세션 advance는 200과 viewType=SCENE, sceneOrder 최소 장면을 반환하고 이후 GET에서 current_scene_id가 세팅된다") {
                val (guardianId, token) = authorizedGuardian()
                val childId = "child-${uniqueSuffix()}"
                val storyId = "story-${uniqueSuffix()}"
                val sessionId = "session-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))
                testStoryFixture.saveStory(storyEntity(storyId))
                testStoryFixture.saveScene(narrationEntity(storyId, 1))
                testStoryFixture.saveScene(dialogueEntity(storyId, 3))
                testSpeakingSessionFixture.save(sessionEntity(sessionId, childId, storyId, currentSceneId = null))
                val firstSceneId = "sc-1-$storyId"

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .`when`()
                    .post("/speaking-sessions/$sessionId/advance")
                    .then()
                    .statusCode(200)
                    .body("success", equalTo(true))
                    .body("code", equalTo(200))
                    .body("data.viewType", equalTo("SCENE"))
                    .body("data.intro", nullValue())
                    .body("data.scene.sceneId", equalTo(firstSceneId))
                    .body("data.scene.sceneOrder", equalTo(1))
                    .body("data.scene.sceneType", equalTo(SceneType.NARRATION.name))
                    .body("data.scene.characterOpeningAudio", nullValue())
                    .body("data.scene.characterClosingAudio", nullValue())

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .`when`()
                    .get("/speaking-sessions/$sessionId")
                    .then()
                    .statusCode(200)
                    .body("data.viewType", equalTo("SCENE"))
                    .body("data.scene.sceneId", equalTo(firstSceneId))
            }

            test("장면 상태 세션 GET은 200과 viewType=SCENE, 그 장면을 반환한다") {
                val (guardianId, token) = authorizedGuardian()
                val childId = "child-${uniqueSuffix()}"
                val storyId = "story-${uniqueSuffix()}"
                val sessionId = "session-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))
                testStoryFixture.saveStory(storyEntity(storyId))
                testStoryFixture.saveScene(narrationEntity(storyId, 1))
                testStoryFixture.saveScene(dialogueEntity(storyId, 3))
                val dialogueSceneId = "sc-3-$storyId"
                testSpeakingSessionFixture.save(
                    sessionEntity(sessionId, childId, storyId, currentSceneId = dialogueSceneId),
                )

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .`when`()
                    .get("/speaking-sessions/$sessionId")
                    .then()
                    .statusCode(200)
                    .body("data.viewType", equalTo("SCENE"))
                    .body("data.intro", nullValue())
                    .body("data.scene.sceneId", equalTo(dialogueSceneId))
                    .body("data.scene.sceneType", equalTo(SceneType.DIALOGUE.name))
                    .body("data.scene.characterDisplayName", equalTo("방귀쟁이 며느리"))
                    .body("data.scene.maxTurns", equalTo(4))
                    .body("data.scene.characterOpening", nullValue())
                    .body("data.scene.characterClosing", nullValue())
                    .body("data.scene.characterOpeningAudio", nullValue())
                    .body("data.scene.characterClosingAudio", nullValue())
                    .body("data.scene.imageUrl", equalTo("/files/$storyId-scene-3.png"))
                    .body("data.scene.characterImageUrl", equalTo("/files/char-ch_banggui_daughter_in_law.png"))
            }

            test("CHARACTER_LINE 장면 세션 GET은 개인화된 대사와 characterOpeningAudio를 반환하고 closingAudio는 null이다") {
                val (guardianId, token) = authorizedGuardian()
                val childId = "child-${uniqueSuffix()}"
                val storyId = "story-${uniqueSuffix()}"
                val sessionId = "session-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))
                testStoryFixture.saveStory(storyEntity(storyId))
                testStoryFixture.saveScene(narrationEntity(storyId, 1))
                testStoryFixture.saveScene(characterLineEntity(storyId, 2))
                testStoryFixture.saveScene(dialogueEntity(storyId, 3))
                val characterLineSceneId = "sc-2-$storyId"
                testSpeakingSessionFixture.save(
                    sessionEntity(sessionId, childId, storyId, currentSceneId = characterLineSceneId),
                )

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .`when`()
                    .get("/speaking-sessions/$sessionId")
                    .then()
                    .statusCode(200)
                    .body("data.viewType", equalTo("SCENE"))
                    .body("data.scene.sceneId", equalTo(characterLineSceneId))
                    .body("data.scene.sceneType", equalTo(SceneType.CHARACTER_LINE.name))
                    .body("data.scene.characterOpening", equalTo("토토야, 내 이야기를 들어 줄래?"))
                    .body("data.scene.characterOpeningAudio", not(nullValue()))
                    .body("data.scene.characterClosingAudio", nullValue())
                    .body("data.scene.characterImageUrl", equalTo("/files/char-ch_banggui_daughter_in_law.png"))
            }

            test("CHARACTER_LINE 장면 세션 advance는 종료 사유 없이도 200과 다음 장면 SCENE 뷰를 반환한다") {
                val (guardianId, token) = authorizedGuardian()
                val childId = "child-${uniqueSuffix()}"
                val storyId = "story-${uniqueSuffix()}"
                val sessionId = "session-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))
                testStoryFixture.saveStory(storyEntity(storyId))
                testStoryFixture.saveScene(characterLineEntity(storyId, 2))
                testStoryFixture.saveScene(dialogueEntity(storyId, 3))
                val characterLineSceneId = "sc-2-$storyId"
                val nextSceneId = "sc-3-$storyId"
                testSpeakingSessionFixture.save(
                    sessionEntity(sessionId, childId, storyId, currentSceneId = characterLineSceneId),
                )

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .`when`()
                    .post("/speaking-sessions/$sessionId/advance")
                    .then()
                    .statusCode(200)
                    .body("data.viewType", equalTo("SCENE"))
                    .body("data.scene.sceneId", equalTo(nextSceneId))
                    .body("data.scene.sceneType", equalTo(SceneType.DIALOGUE.name))

                val storedSession = testSpeakingSessionFixture.findBySessionId(sessionId)
                storedSession?.currentSceneId shouldBe nextSceneId
            }

            test("전개(NARRATION) 장면 세션 advance는 200과 다음 장면 SCENE 뷰를 반환한다") {
                val (guardianId, token) = authorizedGuardian()
                val childId = "child-${uniqueSuffix()}"
                val storyId = "story-${uniqueSuffix()}"
                val sessionId = "session-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))
                testStoryFixture.saveStory(storyEntity(storyId))
                testStoryFixture.saveScene(narrationEntity(storyId, 1))
                testStoryFixture.saveScene(dialogueEntity(storyId, 3))
                val narrationSceneId = "sc-1-$storyId"
                val nextSceneId = "sc-3-$storyId"
                testSpeakingSessionFixture.save(
                    sessionEntity(sessionId, childId, storyId, currentSceneId = narrationSceneId),
                )

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .`when`()
                    .post("/speaking-sessions/$sessionId/advance")
                    .then()
                    .statusCode(200)
                    .body("data.viewType", equalTo("SCENE"))
                    .body("data.intro", nullValue())
                    .body("data.scene.sceneId", equalTo(nextSceneId))
                    .body("data.scene.sceneOrder", equalTo(3))
                    .body("data.scene.sceneType", equalTo(SceneType.DIALOGUE.name))
                    .body("data.scene.characterOpeningAudio", nullValue())
                    .body("data.scene.characterClosingAudio", nullValue())

                val storedSession = testSpeakingSessionFixture.findBySessionId(sessionId)
                storedSession?.currentSceneId shouldBe nextSceneId
                storedSession?.status shouldBe SessionStatus.IN_PROGRESS.name
            }

            test("종료된 대화 장면 세션 advance는 200과 다음 장면 SCENE 뷰를 반환하고 장면 범위 상태를 0으로 초기화한다") {
                val (guardianId, token) = authorizedGuardian()
                val childId = "child-${uniqueSuffix()}"
                val storyId = "story-${uniqueSuffix()}"
                val sessionId = "session-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))
                testStoryFixture.saveStory(storyEntity(storyId))
                testStoryFixture.saveScene(dialogueEntity(storyId, 1))
                testStoryFixture.saveScene(narrationEntity(storyId, 3))
                val dialogueSceneId = "sc-1-$storyId"
                val nextSceneId = "sc-3-$storyId"
                testSpeakingSessionFixture.save(
                    sessionEntity(
                        sessionId,
                        childId,
                        storyId,
                        currentSceneId = dialogueSceneId,
                        accumulatedElements = listOf(ThinkingElement.EMOTION, ThinkingElement.PERSPECTIVE),
                        currentChildTurnCount = 4,
                        turnsWithoutNewElement = 2,
                        sceneEndReason = SceneEndReason.MAX_TURNS.name,
                    ),
                )

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .`when`()
                    .post("/speaking-sessions/$sessionId/advance")
                    .then()
                    .statusCode(200)
                    .body("data.viewType", equalTo("SCENE"))
                    .body("data.scene.sceneId", equalTo(nextSceneId))
                    .body("data.scene.sceneType", equalTo(SceneType.NARRATION.name))
                    .body("data.scene.characterOpeningAudio", nullValue())
                    .body("data.scene.characterClosingAudio", nullValue())

                val storedSession = testSpeakingSessionFixture.findBySessionId(sessionId)
                storedSession?.currentSceneId shouldBe nextSceneId
                storedSession?.accumulatedElements shouldBe emptyList()
                storedSession?.currentChildTurnCount shouldBe 0
                storedSession?.turnsWithoutNewElement shouldBe 0
                storedSession?.sceneEndReason shouldBe null
                storedSession?.status shouldBe SessionStatus.IN_PROGRESS.name
            }

            test("마지막 장면 세션 advance는 200과 viewType=POST_ACTIVITY를 반환하고 세션 status를 POST_ACTIVITY로 전환한다") {
                val (guardianId, token) = authorizedGuardian()
                val childId = "child-${uniqueSuffix()}"
                val storyId = "story-${uniqueSuffix()}"
                val sessionId = "session-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))
                testStoryFixture.saveStory(storyEntity(storyId))
                testStoryFixture.saveScene(narrationEntity(storyId, 1))
                val lastSceneId = "sc-1-$storyId"
                testSpeakingSessionFixture.save(
                    sessionEntity(sessionId, childId, storyId, currentSceneId = lastSceneId),
                )

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .`when`()
                    .post("/speaking-sessions/$sessionId/advance")
                    .then()
                    .statusCode(200)
                    .body("data.viewType", equalTo("POST_ACTIVITY"))
                    .body("data.intro", nullValue())
                    .body("data.scene", nullValue())

                val storedSession = testSpeakingSessionFixture.findBySessionId(sessionId)
                storedSession?.status shouldBe SessionStatus.POST_ACTIVITY.name
            }
        }

        context("예외케이스") {
            test("POST_ACTIVITY status 세션 advance는 422와 BUSINESS_RULE_VIOLATION을 반환하고 lastActivityAt이 불변이다") {
                val (guardianId, token) = authorizedGuardian()
                val childId = "child-${uniqueSuffix()}"
                val storyId = "story-${uniqueSuffix()}"
                val sessionId = "session-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))
                testStoryFixture.saveStory(storyEntity(storyId))
                testStoryFixture.saveScene(narrationEntity(storyId, 1))
                val lastSceneId = "sc-1-$storyId"
                val savedEntity = testSpeakingSessionFixture.save(
                    sessionEntity(
                        sessionId,
                        childId,
                        storyId,
                        currentSceneId = lastSceneId,
                        status = SessionStatus.POST_ACTIVITY,
                    ),
                )

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .`when`()
                    .post("/speaking-sessions/$sessionId/advance")
                    .then()
                    .statusCode(422)
                    .body("success", equalTo(false))
                    .body("code", equalTo(422))
                    .body("detailCode", equalTo(ExceptionResponseCode.BUSINESS_RULE_VIOLATION.detailCode))

                val storedSession = testSpeakingSessionFixture.findBySessionId(sessionId)
                storedSession?.lastActivityAt shouldBe savedEntity.lastActivityAt
                storedSession?.status shouldBe SessionStatus.POST_ACTIVITY.name
            }

            test("종료되지 않은 대화 장면 세션 advance는 422와 BUSINESS_RULE_VIOLATION을 반환한다") {
                val (guardianId, token) = authorizedGuardian()
                val childId = "child-${uniqueSuffix()}"
                val storyId = "story-${uniqueSuffix()}"
                val sessionId = "session-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))
                testStoryFixture.saveStory(storyEntity(storyId))
                testStoryFixture.saveScene(dialogueEntity(storyId, 3))
                val dialogueSceneId = "sc-3-$storyId"
                testSpeakingSessionFixture.save(
                    sessionEntity(sessionId, childId, storyId, currentSceneId = dialogueSceneId, sceneEndReason = null),
                )

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .`when`()
                    .post("/speaking-sessions/$sessionId/advance")
                    .then()
                    .statusCode(422)
                    .body("success", equalTo(false))
                    .body("code", equalTo(422))
                    .body("detailCode", equalTo(ExceptionResponseCode.BUSINESS_RULE_VIOLATION.detailCode))
            }

            test("남의 아이 세션 GET은 404와 NOT_FOUND로 은닉한다") {
                val (_, token) = authorizedGuardian()
                val otherGuardianId = "other-${uniqueSuffix()}"
                testUserFixture.saveUser(userEntity(otherGuardianId))
                val otherChildId = "child-${uniqueSuffix()}"
                val storyId = "story-${uniqueSuffix()}"
                val sessionId = "session-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(otherChildId, otherGuardianId))
                testStoryFixture.saveStory(storyEntity(storyId))
                testSpeakingSessionFixture.save(sessionEntity(sessionId, otherChildId, storyId, currentSceneId = null))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .`when`()
                    .get("/speaking-sessions/$sessionId")
                    .then()
                    .statusCode(404)
                    .body("detailCode", equalTo(ExceptionResponseCode.NOT_FOUND.detailCode))
            }

            test("남의 아이 세션 advance는 404와 NOT_FOUND로 은닉한다") {
                val (_, token) = authorizedGuardian()
                val otherGuardianId = "other-${uniqueSuffix()}"
                testUserFixture.saveUser(userEntity(otherGuardianId))
                val otherChildId = "child-${uniqueSuffix()}"
                val storyId = "story-${uniqueSuffix()}"
                val sessionId = "session-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(otherChildId, otherGuardianId))
                testStoryFixture.saveStory(storyEntity(storyId))
                testStoryFixture.saveScene(narrationEntity(storyId, 1))
                testSpeakingSessionFixture.save(sessionEntity(sessionId, otherChildId, storyId, currentSceneId = null))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .`when`()
                    .post("/speaking-sessions/$sessionId/advance")
                    .then()
                    .statusCode(404)
                    .body("detailCode", equalTo(ExceptionResponseCode.NOT_FOUND.detailCode))
            }

            test("존재하지 않는 세션 GET은 404와 NOT_FOUND를 반환한다") {
                val (_, token) = authorizedGuardian()

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .`when`()
                    .get("/speaking-sessions/missing-${uniqueSuffix()}")
                    .then()
                    .statusCode(404)
                    .body("detailCode", equalTo(ExceptionResponseCode.NOT_FOUND.detailCode))
            }

            test("토큰이 없으면 GET은 401과 EMPTY_TOKEN을 반환한다") {
                RestAssured.given()
                    .contentType(ContentType.JSON)
                    .`when`()
                    .get("/speaking-sessions/any-session")
                    .then()
                    .statusCode(401)
                    .body("detailCode", equalTo(ExceptionResponseCode.EMPTY_TOKEN.detailCode))
                    .body("message", equalTo("토큰이 없습니다."))
            }

            test("토큰이 없으면 advance는 401과 EMPTY_TOKEN을 반환한다") {
                RestAssured.given()
                    .contentType(ContentType.JSON)
                    .`when`()
                    .post("/speaking-sessions/any-session/advance")
                    .then()
                    .statusCode(401)
                    .body("detailCode", equalTo(ExceptionResponseCode.EMPTY_TOKEN.detailCode))
                    .body("message", equalTo("토큰이 없습니다."))
            }
        }
    }

    context("getResumableSessions") {
        fun sessionEntityWithLastActivity(
            sessionId: String,
            childId: String,
            storyId: String,
            status: SessionStatus = SessionStatus.IN_PROGRESS,
            lastActivityAt: LocalDateTime,
            currentSceneId: String? = null,
        ): SpeakingSessionOrmEntity = SpeakingSessionOrmEntity(
            sessionId = sessionId,
            childId = childId,
            storyId = storyId,
            currentSceneId = currentSceneId,
            status = status.name,
            startedAt = LocalDateTime.now().minusMinutes(10),
            lastActivityAt = lastActivityAt,
            accumulatedElements = emptyList(),
            currentChildTurnCount = 0,
            turnsWithoutNewElement = 0,
            sceneEndReason = null,
        )

        fun topicEntity(storyId: String, topic: String): StoryTopicOrmEntity = StoryTopicOrmEntity(
            id = "topic-$topic-$storyId",
            storyId = storyId,
            topic = topic,
        )

        context("성공") {
            test("이어하기 응답에 스토리 표시정보와 진행도가 채워지고 세션마다 올바른 스토리로 매칭된다") {
                val (guardianId, token) = authorizedGuardian()
                val childId = "child-${uniqueSuffix()}"
                val storyIdA = "story-a-${uniqueSuffix()}"
                val storyIdB = "story-b-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))
                testStoryFixture.saveStory(storyEntity(storyIdA))
                testStoryFixture.saveStory(storyEntity(storyIdB))
                testStoryFixture.saveScene(narrationEntity(storyIdA, 1, chapter = 1))
                testStoryFixture.saveScene(narrationEntity(storyIdA, 2, chapter = 2))
                testStoryFixture.saveScene(dialogueEntity(storyIdA, 3, chapter = 3))
                testStoryFixture.saveScene(narrationEntity(storyIdB, 1, chapter = 1))
                testStoryFixture.saveTopic(topicEntity(storyIdA, "공감"))
                testStoryFixture.saveTopic(topicEntity(storyIdA, "존중"))
                val sceneIdChapterTwo = "sc-2-$storyIdA"
                val recentAt = LocalDateTime.now().minusMinutes(1)
                val olderAt = LocalDateTime.now().minusMinutes(3)
                val sessionIdA = "session-a-${uniqueSuffix()}"
                val sessionIdB = "session-b-${uniqueSuffix()}"
                testSpeakingSessionFixture.save(
                    sessionEntityWithLastActivity(
                        sessionIdA,
                        childId,
                        storyIdA,
                        lastActivityAt = recentAt,
                        currentSceneId = sceneIdChapterTwo,
                    ),
                )
                testSpeakingSessionFixture.save(
                    sessionEntityWithLastActivity(
                        sessionIdB,
                        childId,
                        storyIdB,
                        lastActivityAt = olderAt,
                        currentSceneId = null,
                    ),
                )

                RestAssured.given()
                    .header("Authorization", token)
                    .`when`()
                    .get("/speaking-sessions?childId=$childId")
                    .then()
                    .statusCode(200)
                    .body("data.size()", equalTo(2))
                    .body("data[0].sessionId", equalTo(sessionIdA))
                    .body("data[0].storyId", equalTo(storyIdA))
                    .body("data[0].status", equalTo(SessionStatus.IN_PROGRESS.name))
                    .body("data[0].currentSceneId", equalTo(sceneIdChapterTwo))
                    .body("data[0].title", equalTo("제목-$storyIdA"))
                    .body("data[0].representativeImageUrl", equalTo("/files/$storyIdA.png"))
                    .body("data[0].difficulty", equalTo("보통"))
                    .body("data[0].topics", containsInAnyOrder("공감", "존중"))
                    .body("data[0].currentChapter", equalTo(2))
                    .body("data[0].totalChapters", equalTo(3))
                    .body("data[1].sessionId", equalTo(sessionIdB))
                    .body("data[1].storyId", equalTo(storyIdB))
                    .body("data[1].title", equalTo("제목-$storyIdB"))
                    .body("data[1].currentChapter", equalTo(0))
                    .body("data[1].totalChapters", equalTo(1))
            }

            test("in_progress 2건과 completed 1건이 있을 때 GET ?childId는 200과 in_progress 2건만 lastActivityAt 최근순으로 반환한다") {
                val (guardianId, token) = authorizedGuardian()
                val childId = "child-${uniqueSuffix()}"
                val storyId = "story-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))
                testStoryFixture.saveStory(storyEntity(storyId))
                val recentAt = LocalDateTime.now().minusMinutes(1)
                val olderAt = LocalDateTime.now().minusMinutes(3)
                val sessionIdRecent = "session-a-${uniqueSuffix()}"
                val sessionIdOlder = "session-b-${uniqueSuffix()}"
                val sessionIdCompleted = "session-c-${uniqueSuffix()}"
                testSpeakingSessionFixture.save(
                    sessionEntityWithLastActivity(sessionIdRecent, childId, storyId, lastActivityAt = recentAt),
                )
                testSpeakingSessionFixture.save(
                    sessionEntityWithLastActivity(sessionIdOlder, childId, storyId, lastActivityAt = olderAt),
                )
                testSpeakingSessionFixture.save(
                    sessionEntityWithLastActivity(
                        sessionIdCompleted,
                        childId,
                        storyId,
                        status = SessionStatus.COMPLETED,
                        lastActivityAt = LocalDateTime.now(),
                    ),
                )

                RestAssured.given()
                    .header("Authorization", token)
                    .`when`()
                    .get("/speaking-sessions?childId=$childId")
                    .then()
                    .statusCode(200)
                    .body("success", equalTo(true))
                    .body("code", equalTo(200))
                    .body("data.size()", equalTo(2))
                    .body("data[0].sessionId", equalTo(sessionIdRecent))
                    .body("data[0].status", equalTo(SessionStatus.IN_PROGRESS.name))
                    .body("data[1].sessionId", equalTo(sessionIdOlder))
            }

            test("in_progress·post_activity·completed가 섞이면 GET ?childId는 in_progress·post_activity만 반환하고 completed는 제외한다") {
                val (guardianId, token) = authorizedGuardian()
                val childId = "child-${uniqueSuffix()}"
                val storyId = "story-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))
                testStoryFixture.saveStory(storyEntity(storyId))
                val recentAt = LocalDateTime.now().minusMinutes(1)
                val olderAt = LocalDateTime.now().minusMinutes(3)
                val sessionIdInProgress = "session-a-${uniqueSuffix()}"
                val sessionIdPostActivity = "session-b-${uniqueSuffix()}"
                val sessionIdCompleted = "session-c-${uniqueSuffix()}"
                testSpeakingSessionFixture.save(
                    sessionEntityWithLastActivity(sessionIdInProgress, childId, storyId, lastActivityAt = recentAt),
                )
                testSpeakingSessionFixture.save(
                    sessionEntityWithLastActivity(
                        sessionIdPostActivity,
                        childId,
                        storyId,
                        status = SessionStatus.POST_ACTIVITY,
                        lastActivityAt = olderAt,
                    ),
                )
                testSpeakingSessionFixture.save(
                    sessionEntityWithLastActivity(
                        sessionIdCompleted,
                        childId,
                        storyId,
                        status = SessionStatus.COMPLETED,
                        lastActivityAt = LocalDateTime.now(),
                    ),
                )

                RestAssured.given()
                    .header("Authorization", token)
                    .`when`()
                    .get("/speaking-sessions?childId=$childId")
                    .then()
                    .statusCode(200)
                    .body("data.size()", equalTo(2))
                    .body("data[0].sessionId", equalTo(sessionIdInProgress))
                    .body("data[0].status", equalTo(SessionStatus.IN_PROGRESS.name))
                    .body("data[1].sessionId", equalTo(sessionIdPostActivity))
                    .body("data[1].status", equalTo(SessionStatus.POST_ACTIVITY.name))
            }

            test("나간(stop) IN_PROGRESS 세션이 이어하기 목록에 뜨고, 그 세션에 발화 제출이 재활성화 없이 201로 통과한다") {
                val (guardianId, token) = authorizedGuardian()
                val childId = "child-${uniqueSuffix()}"
                val storyId = "story-${uniqueSuffix()}"
                val sessionId = "session-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))
                testStoryFixture.saveStory(storyEntity(storyId))
                testStoryFixture.saveScene(dialogueEntity(storyId, 3))
                val dialogueSceneId = "sc-3-$storyId"
                testSpeakingSessionFixture.save(
                    sessionEntity(sessionId, childId, storyId, currentSceneId = dialogueSceneId),
                )

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .`when`()
                    .post("/speaking-sessions/$sessionId/stop")
                    .then()
                    .statusCode(200)
                    .body("data.status", equalTo(SessionStatus.IN_PROGRESS.name))

                RestAssured.given()
                    .header("Authorization", token)
                    .`when`()
                    .get("/speaking-sessions?childId=$childId")
                    .then()
                    .statusCode(200)
                    .body("data.size()", equalTo(1))
                    .body("data[0].sessionId", equalTo(sessionId))
                    .body("data[0].status", equalTo(SessionStatus.IN_PROGRESS.name))

                RestAssured.given()
                    .header("Authorization", token)
                    .multiPart(
                        MultiPartSpecBuilder("""{"text":"며느리가 참 힘들었겠어요"}""".toByteArray(StandardCharsets.UTF_8))
                            .controlName("request")
                            .mimeType("application/json")
                            .charset(StandardCharsets.UTF_8)
                            .build(),
                    )
                    .`when`()
                    .post("/speaking-sessions/$sessionId/utterances")
                    .then()
                    .statusCode(201)
                    .body("data.speakerType", equalTo("CHILD"))
                    .body("data.sceneId", equalTo(dialogueSceneId))
                    .body("data.turnOrder", equalTo(1))

                val storedSession = testSpeakingSessionFixture.findBySessionId(sessionId)
                storedSession?.status shouldBe SessionStatus.IN_PROGRESS.name
            }

            test("in_progress 세션이 없으면 200과 빈 배열을 반환한다") {
                val (guardianId, token) = authorizedGuardian()
                val childId = "child-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))

                RestAssured.given()
                    .header("Authorization", token)
                    .`when`()
                    .get("/speaking-sessions?childId=$childId")
                    .then()
                    .statusCode(200)
                    .body("success", equalTo(true))
                    .body("data.size()", equalTo(0))
            }
        }

        context("예외케이스") {
            test("남의 아이 childId로 조회하면 404와 NOT_FOUND를 반환한다") {
                val (_, token) = authorizedGuardian()
                val otherGuardianId = "other-${uniqueSuffix()}"
                testUserFixture.saveUser(userEntity(otherGuardianId))
                val otherChildId = "child-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(otherChildId, otherGuardianId))

                RestAssured.given()
                    .header("Authorization", token)
                    .`when`()
                    .get("/speaking-sessions?childId=$otherChildId")
                    .then()
                    .statusCode(404)
                    .body("detailCode", equalTo(ExceptionResponseCode.NOT_FOUND.detailCode))
            }

            test("토큰 없이 조회하면 401과 EMPTY_TOKEN을 반환한다") {
                RestAssured.given()
                    .`when`()
                    .get("/speaking-sessions?childId=any-child")
                    .then()
                    .statusCode(401)
                    .body("detailCode", equalTo(ExceptionResponseCode.EMPTY_TOKEN.detailCode))
                    .body("message", equalTo("토큰이 없습니다."))
            }
        }
    }

    context("submitUtterance") {
        fun utteranceRequestPart(text: String, sttRawText: String? = null): MultiPartSpecification {
            val json = if (sttRawText != null) {
                """{"text":"$text","sttRawText":"$sttRawText"}"""
            } else {
                """{"text":"$text"}"""
            }
            return MultiPartSpecBuilder(json.toByteArray(StandardCharsets.UTF_8))
                .controlName("request")
                .mimeType("application/json")
                .charset(StandardCharsets.UTF_8)
                .build()
        }

        context("성공") {
            test("대화 장면 진행 중 세션에 유효 발화를 제출하면 201과 child 메시지 1건을 저장한다") {
                val (guardianId, token) = authorizedGuardian()
                val childId = "child-${uniqueSuffix()}"
                val storyId = "story-${uniqueSuffix()}"
                val sessionId = "session-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))
                testStoryFixture.saveStory(storyEntity(storyId))
                testStoryFixture.saveScene(dialogueEntity(storyId, 3))
                val dialogueSceneId = "sc-3-$storyId"
                testSpeakingSessionFixture.save(
                    sessionEntity(sessionId, childId, storyId, currentSceneId = dialogueSceneId),
                )

                RestAssured.given()
                    .header("Authorization", token)
                    .multiPart(utteranceRequestPart("며느리가 참 힘들었겠어요", "며느리가 참 힘드러껬어요"))
                    .`when`()
                    .post("/speaking-sessions/$sessionId/utterances")
                    .then()
                    .statusCode(201)
                    .body("success", equalTo(true))
                    .body("code", equalTo(201))
                    .body("message", equalTo(SuccessResponseCode.CREATED.message))
                    .body("data.speakerType", equalTo("CHILD"))
                    .body("data.sceneId", equalTo(dialogueSceneId))
                    .body("data.turnOrder", equalTo(1))
                    .body("data.text", equalTo("며느리가 참 힘들었겠어요"))
                    .body("data.validity", equalTo("VALID"))
                    .body("data.detectedElements[0].type", equalTo("EMOTION"))
                    .body("data.detectedElements[0].evidence", equalTo("힘들"))
                    .body("data.accumulatedElements", equalTo(listOf("EMOTION")))
                    .body("data.missingElements", equalTo(listOf("PERSPECTIVE")))
                    .body("data.mode", equalTo("NORMAL"))
                    .body("data.sceneEndReason", nullValue())
                    .body("data.sceneGoalMet", equalTo(false))
                    .body("data.guidanceTarget", nullValue())
                    .body("data.characterReply.speakerType", equalTo("CHARACTER"))
                    .body("data.characterReply.turnOrder", equalTo(2))
                    .body("data.characterReply.text", not(emptyOrNullString()))
                    .body("data.characterReply.characterReplyAudio", not(nullValue()))

                testMessageFixture.countBySessionId(sessionId) shouldBe 2L
                val stored = testMessageFixture.findAllBySessionId(sessionId).first()
                stored.speakerType shouldBe "CHILD"
                stored.text shouldBe "며느리가 참 힘들었겠어요"
                stored.sttRawText shouldBe "며느리가 참 힘드러껬어요"
                stored.audioUrl shouldBe null

                testUtteranceAnalysisFixture.count() shouldBe 1L
                val storedAnalysis = testUtteranceAnalysisFixture.findAll().first()
                storedAnalysis.messageId shouldBe stored.id
                storedAnalysis.detectedElements.map { it.type } shouldBe listOf(ThinkingElement.EMOTION)

                val storedSession = testSpeakingSessionFixture.findBySessionId(sessionId)
                storedSession?.accumulatedElements shouldBe listOf(ThinkingElement.EMOTION)
                storedSession?.currentChildTurnCount shouldBe 1
                storedSession?.turnsWithoutNewElement shouldBe 0
                storedSession?.lastResponseMode shouldBe "NORMAL"
                storedSession?.sceneEndReason shouldBe null
            }

            test("발화 음성과 함께 제출하면 201과 저장된 메시지 audioUrl이 /files/ 로 시작한다") {
                val (guardianId, token) = authorizedGuardian()
                val childId = "child-${uniqueSuffix()}"
                val storyId = "story-${uniqueSuffix()}"
                val sessionId = "session-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))
                testStoryFixture.saveStory(storyEntity(storyId))
                testStoryFixture.saveScene(dialogueEntity(storyId, 3))
                val dialogueSceneId = "sc-3-$storyId"
                testSpeakingSessionFixture.save(
                    sessionEntity(sessionId, childId, storyId, currentSceneId = dialogueSceneId),
                )

                RestAssured.given()
                    .header("Authorization", token)
                    .multiPart(utteranceRequestPart("며느리가 참 힘들었겠어요"))
                    .multiPart("audio", "utterance.m4a", "audio-bytes".toByteArray(), "audio/mp4")
                    .`when`()
                    .post("/speaking-sessions/$sessionId/utterances")
                    .then()
                    .statusCode(201)

                val stored = testMessageFixture.findAllBySessionId(sessionId).first()
                stored.audioUrl shouldNotBe null
                stored.audioUrl!!.startsWith("/files/") shouldBe true
            }

            test("음성 없이 발화 텍스트만 제출하면 201과 저장된 메시지 audioUrl은 null이다") {
                val (guardianId, token) = authorizedGuardian()
                val childId = "child-${uniqueSuffix()}"
                val storyId = "story-${uniqueSuffix()}"
                val sessionId = "session-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))
                testStoryFixture.saveStory(storyEntity(storyId))
                testStoryFixture.saveScene(dialogueEntity(storyId, 3))
                val dialogueSceneId = "sc-3-$storyId"
                testSpeakingSessionFixture.save(
                    sessionEntity(sessionId, childId, storyId, currentSceneId = dialogueSceneId),
                )

                RestAssured.given()
                    .header("Authorization", token)
                    .multiPart(utteranceRequestPart("며느리가 참 힘들었겠어요"))
                    .`when`()
                    .post("/speaking-sessions/$sessionId/utterances")
                    .then()
                    .statusCode(201)

                val stored = testMessageFixture.findAllBySessionId(sessionId).first()
                stored.audioUrl shouldBe null
            }

            test("같은 세션에 두 번째 발화를 제출하면 201과 turnOrder=2가 되고 누적 요소가 중복 없이 늘어난다") {
                val (guardianId, token) = authorizedGuardian()
                val childId = "child-${uniqueSuffix()}"
                val storyId = "story-${uniqueSuffix()}"
                val sessionId = "session-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))
                testStoryFixture.saveStory(storyEntity(storyId))
                testStoryFixture.saveScene(dialogueEntity(storyId, 3))
                val dialogueSceneId = "sc-3-$storyId"
                testSpeakingSessionFixture.save(
                    sessionEntity(sessionId, childId, storyId, currentSceneId = dialogueSceneId),
                )

                RestAssured.given()
                    .header("Authorization", token)
                    .multiPart(utteranceRequestPart("며느리가 참 힘들었겠어요"))
                    .`when`()
                    .post("/speaking-sessions/$sessionId/utterances")
                    .then()
                    .statusCode(201)
                    .body("data.turnOrder", equalTo(1))
                    .body("data.accumulatedElements", equalTo(listOf("EMOTION")))
                    .body("data.characterReply.turnOrder", equalTo(2))
                    .body("data.characterReply.text", not(emptyOrNullString()))

                RestAssured.given()
                    .header("Authorization", token)
                    .multiPart(utteranceRequestPart("며느리 입장에서 생각하면 참 힘들었겠어요"))
                    .`when`()
                    .post("/speaking-sessions/$sessionId/utterances")
                    .then()
                    .statusCode(201)
                    .body("data.turnOrder", equalTo(3))
                    .body("data.characterReply.turnOrder", equalTo(4))

                testMessageFixture.countBySessionId(sessionId) shouldBe 4L
                testUtteranceAnalysisFixture.count() shouldBe 2L

                val storedSession = testSpeakingSessionFixture.findBySessionId(sessionId)
                storedSession?.accumulatedElements shouldContainExactlyInAnyOrder listOf(
                    ThinkingElement.EMOTION,
                    ThinkingElement.PERSPECTIVE,
                )
            }

            test("최대턴(maxTurns=4)까지 필수 요소가 남은 채 발화하면 마지막 응답은 mode=CLOSING·sceneEndReason=MAX_TURNS다") {
                val (guardianId, token) = authorizedGuardian()
                val childId = "child-${uniqueSuffix()}"
                val storyId = "story-${uniqueSuffix()}"
                val sessionId = "session-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))
                testStoryFixture.saveStory(storyEntity(storyId))
                testStoryFixture.saveScene(dialogueEntity(storyId, 3))
                val dialogueSceneId = "sc-3-$storyId"
                testSpeakingSessionFixture.save(
                    sessionEntity(sessionId, childId, storyId, currentSceneId = dialogueSceneId),
                )
                val emotionOnlyText = "며느리가 참 힘들었겠어요"

                repeat(3) {
                    RestAssured.given()
                        .header("Authorization", token)
                        .multiPart(utteranceRequestPart(emotionOnlyText))
                        .`when`()
                        .post("/speaking-sessions/$sessionId/utterances")
                        .then()
                        .statusCode(201)
                        .body("data.sceneEndReason", nullValue())
                }

                RestAssured.given()
                    .header("Authorization", token)
                    .multiPart(utteranceRequestPart(emotionOnlyText))
                    .`when`()
                    .post("/speaking-sessions/$sessionId/utterances")
                    .then()
                    .statusCode(201)
                    .body("data.mode", equalTo("CLOSING"))
                    .body("data.sceneEndReason", equalTo("MAX_TURNS"))
                    .body("data.sceneGoalMet", equalTo(false))
                    .body("data.turnOrder", equalTo(7))
                    .body("data.characterReply.speakerType", equalTo("CHARACTER"))
                    .body("data.characterReply.turnOrder", equalTo(8))
                    .body("data.characterReply.text", equalTo("방귀쟁이 며느리: 이야기해 줘서 정말 고마워. 네 덕분에 마음이 놓였어."))

                val storedSession = testSpeakingSessionFixture.findBySessionId(sessionId)
                storedSession?.currentChildTurnCount shouldBe 4
                storedSession?.sceneEndReason shouldBe "MAX_TURNS"
                storedSession?.lastResponseMode shouldBe "CLOSING"
                storedSession?.status shouldBe SessionStatus.IN_PROGRESS.name
            }
        }

        context("예외케이스") {
            test("sceneEndReason이 세팅된 대화 장면 세션에 발화 제출하면 422와 BUSINESS_RULE_VIOLATION을 반환하고 메시지가 증가하지 않는다") {
                val (guardianId, token) = authorizedGuardian()
                val childId = "child-${uniqueSuffix()}"
                val storyId = "story-${uniqueSuffix()}"
                val sessionId = "session-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))
                testStoryFixture.saveStory(storyEntity(storyId))
                testStoryFixture.saveScene(dialogueEntity(storyId, 3))
                val dialogueSceneId = "sc-3-$storyId"
                testSpeakingSessionFixture.save(
                    sessionEntity(
                        sessionId,
                        childId,
                        storyId,
                        currentSceneId = dialogueSceneId,
                        sceneEndReason = SceneEndReason.MAX_TURNS.name,
                    ),
                )

                RestAssured.given()
                    .header("Authorization", token)
                    .multiPart(utteranceRequestPart("며느리가 참 힘들었겠어요"))
                    .`when`()
                    .post("/speaking-sessions/$sessionId/utterances")
                    .then()
                    .statusCode(422)
                    .body("success", equalTo(false))
                    .body("code", equalTo(422))
                    .body("detailCode", equalTo(ExceptionResponseCode.BUSINESS_RULE_VIOLATION.detailCode))

                testMessageFixture.countBySessionId(sessionId) shouldBe 0L
            }

            test("POST_ACTIVITY status 세션에 발화 제출하면 422와 BUSINESS_RULE_VIOLATION을 반환하고 메시지가 증가하지 않는다") {
                val (guardianId, token) = authorizedGuardian()
                val childId = "child-${uniqueSuffix()}"
                val storyId = "story-${uniqueSuffix()}"
                val sessionId = "session-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))
                testStoryFixture.saveStory(storyEntity(storyId))
                testStoryFixture.saveScene(dialogueEntity(storyId, 3))
                val dialogueSceneId = "sc-3-$storyId"
                testSpeakingSessionFixture.save(
                    sessionEntity(
                        sessionId,
                        childId,
                        storyId,
                        currentSceneId = dialogueSceneId,
                        status = SessionStatus.POST_ACTIVITY,
                    ),
                )

                RestAssured.given()
                    .header("Authorization", token)
                    .multiPart(utteranceRequestPart("며느리가 참 힘들었겠어요"))
                    .`when`()
                    .post("/speaking-sessions/$sessionId/utterances")
                    .then()
                    .statusCode(422)
                    .body("success", equalTo(false))
                    .body("code", equalTo(422))
                    .body("detailCode", equalTo(ExceptionResponseCode.BUSINESS_RULE_VIOLATION.detailCode))

                testMessageFixture.countBySessionId(sessionId) shouldBe 0L
            }

            test("공백 발화 텍스트는 400과 INVALID_DTO_PARAMETER를 반환하고 메시지가 생성되지 않는다") {
                val (guardianId, token) = authorizedGuardian()
                val childId = "child-${uniqueSuffix()}"
                val storyId = "story-${uniqueSuffix()}"
                val sessionId = "session-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))
                testStoryFixture.saveStory(storyEntity(storyId))
                testStoryFixture.saveScene(dialogueEntity(storyId, 3))
                val dialogueSceneId = "sc-3-$storyId"
                testSpeakingSessionFixture.save(
                    sessionEntity(sessionId, childId, storyId, currentSceneId = dialogueSceneId),
                )

                RestAssured.given()
                    .header("Authorization", token)
                    .multiPart(utteranceRequestPart("   "))
                    .`when`()
                    .post("/speaking-sessions/$sessionId/utterances")
                    .then()
                    .statusCode(400)
                    .body("success", equalTo(false))
                    .body("code", equalTo(400))
                    .body("detailCode", equalTo(ExceptionResponseCode.INVALID_DTO_PARAMETER.detailCode))

                testMessageFixture.countBySessionId(sessionId) shouldBe 0L
                testUtteranceAnalysisFixture.count() shouldBe 0L
            }

            test("sttRawText 없이 발화 텍스트만 제출하면 201과 message.sttRawText null로 저장된다") {
                val (guardianId, token) = authorizedGuardian()
                val childId = "child-${uniqueSuffix()}"
                val storyId = "story-${uniqueSuffix()}"
                val sessionId = "session-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))
                testStoryFixture.saveStory(storyEntity(storyId))
                testStoryFixture.saveScene(dialogueEntity(storyId, 3))
                val dialogueSceneId = "sc-3-$storyId"
                testSpeakingSessionFixture.save(
                    sessionEntity(sessionId, childId, storyId, currentSceneId = dialogueSceneId),
                )

                RestAssured.given()
                    .header("Authorization", token)
                    .multiPart(utteranceRequestPart("며느리가 참 힘들었겠어요"))
                    .`when`()
                    .post("/speaking-sessions/$sessionId/utterances")
                    .then()
                    .statusCode(201)
                    .body("data.text", equalTo("며느리가 참 힘들었겠어요"))

                val stored = testMessageFixture.findAllBySessionId(sessionId).first()
                stored.text shouldBe "며느리가 참 힘들었겠어요"
                stored.sttRawText shouldBe null
            }

            test("도입 상태(current_scene_id null) 세션에 제출하면 422와 BUSINESS_RULE_VIOLATION을 반환하고 미생성이다") {
                val (guardianId, token) = authorizedGuardian()
                val childId = "child-${uniqueSuffix()}"
                val storyId = "story-${uniqueSuffix()}"
                val sessionId = "session-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))
                testStoryFixture.saveStory(storyEntity(storyId))
                testStoryFixture.saveScene(dialogueEntity(storyId, 3))
                testSpeakingSessionFixture.save(sessionEntity(sessionId, childId, storyId, currentSceneId = null))

                RestAssured.given()
                    .header("Authorization", token)
                    .multiPart(utteranceRequestPart("며느리가 참 힘들었겠어요"))
                    .`when`()
                    .post("/speaking-sessions/$sessionId/utterances")
                    .then()
                    .statusCode(422)
                    .body("detailCode", equalTo(ExceptionResponseCode.BUSINESS_RULE_VIOLATION.detailCode))

                testMessageFixture.countBySessionId(sessionId) shouldBe 0L
            }

            test("비대화(NARRATION) 장면 세션에 제출하면 422와 BUSINESS_RULE_VIOLATION을 반환하고 미생성이다") {
                val (guardianId, token) = authorizedGuardian()
                val childId = "child-${uniqueSuffix()}"
                val storyId = "story-${uniqueSuffix()}"
                val sessionId = "session-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))
                testStoryFixture.saveStory(storyEntity(storyId))
                testStoryFixture.saveScene(narrationEntity(storyId, 1))
                val narrationSceneId = "sc-1-$storyId"
                testSpeakingSessionFixture.save(
                    sessionEntity(sessionId, childId, storyId, currentSceneId = narrationSceneId),
                )

                RestAssured.given()
                    .header("Authorization", token)
                    .multiPart(utteranceRequestPart("며느리가 참 힘들었겠어요"))
                    .`when`()
                    .post("/speaking-sessions/$sessionId/utterances")
                    .then()
                    .statusCode(422)
                    .body("detailCode", equalTo(ExceptionResponseCode.BUSINESS_RULE_VIOLATION.detailCode))

                testMessageFixture.countBySessionId(sessionId) shouldBe 0L
            }

            test("남의 아이 세션에 제출하면 404와 NOT_FOUND로 은닉하고 미생성이다") {
                val (_, token) = authorizedGuardian()
                val otherGuardianId = "other-${uniqueSuffix()}"
                testUserFixture.saveUser(userEntity(otherGuardianId))
                val otherChildId = "child-${uniqueSuffix()}"
                val storyId = "story-${uniqueSuffix()}"
                val sessionId = "session-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(otherChildId, otherGuardianId))
                testStoryFixture.saveStory(storyEntity(storyId))
                testStoryFixture.saveScene(dialogueEntity(storyId, 3))
                val dialogueSceneId = "sc-3-$storyId"
                testSpeakingSessionFixture.save(
                    sessionEntity(sessionId, otherChildId, storyId, currentSceneId = dialogueSceneId),
                )

                RestAssured.given()
                    .header("Authorization", token)
                    .multiPart(utteranceRequestPart("며느리가 참 힘들었겠어요"))
                    .`when`()
                    .post("/speaking-sessions/$sessionId/utterances")
                    .then()
                    .statusCode(404)
                    .body("detailCode", equalTo(ExceptionResponseCode.NOT_FOUND.detailCode))

                testMessageFixture.countBySessionId(sessionId) shouldBe 0L
            }

            test("존재하지 않는 세션에 제출하면 404와 NOT_FOUND를 반환한다") {
                val (_, token) = authorizedGuardian()

                RestAssured.given()
                    .header("Authorization", token)
                    .multiPart(utteranceRequestPart("며느리가 참 힘들었겠어요"))
                    .`when`()
                    .post("/speaking-sessions/missing-${uniqueSuffix()}/utterances")
                    .then()
                    .statusCode(404)
                    .body("detailCode", equalTo(ExceptionResponseCode.NOT_FOUND.detailCode))
            }

            test("토큰이 없으면 401과 EMPTY_TOKEN을 반환한다") {
                RestAssured.given()
                    .multiPart(utteranceRequestPart("any"))
                    .`when`()
                    .post("/speaking-sessions/any-session/utterances")
                    .then()
                    .statusCode(401)
                    .body("detailCode", equalTo(ExceptionResponseCode.EMPTY_TOKEN.detailCode))
                    .body("message", equalTo("토큰이 없습니다."))
            }
        }
    }

    context("submitCardOrder") {
        val cardA = PostActivityConfig.Card(id = "card-a", text = "A 장면", correctOrder = 1)
        val cardB = PostActivityConfig.Card(id = "card-b", text = "B 장면", correctOrder = 2)
        val cardC = PostActivityConfig.Card(id = "card-c", text = "C 장면", correctOrder = 3)
        val retellingKeywords = listOf("방귀", "며느리", "시아버지")
        val testConfig = PostActivityConfig(
            cards = listOf(cardC, cardA, cardB),
            retellingKeywords = retellingKeywords,
        )
        val correctOrderPayload = listOf("card-a", "card-b", "card-c")
        val wrongOrderPayload = listOf("card-c", "card-b", "card-a")

        context("성공") {
            test("POST_ACTIVITY 세션에 정답 순서를 제출하면 200과 isOrderCorrect=true·retellingKeywords를 반환하고 post_activity_results 1건이 저장된다") {
                val (guardianId, token) = authorizedGuardian()
                val childId = "child-${uniqueSuffix()}"
                val storyId = "story-${uniqueSuffix()}"
                val sessionId = "session-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))
                testStoryFixture.saveStory(storyEntity(storyId, postActivityConfig = testConfig))
                testSpeakingSessionFixture.save(
                    sessionEntity(sessionId, childId, storyId, status = SessionStatus.POST_ACTIVITY),
                )

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .body(mapOf("order" to correctOrderPayload))
                    .`when`()
                    .post("/speaking-sessions/$sessionId/post-activity/card-order")
                    .then()
                    .statusCode(200)
                    .body("success", equalTo(true))
                    .body("code", equalTo(200))
                    .body("message", equalTo(SuccessResponseCode.OK.message))
                    .body("data.isOrderCorrect", equalTo(true))
                    .body("data.attemptCount", equalTo(1))
                    .body("data.retellingKeywords", equalTo(retellingKeywords))

                testPostActivityResultFixture.count() shouldBe 1L
                val stored = testPostActivityResultFixture.findBySessionId(sessionId)
                stored?.isOrderCorrect shouldBe true
                stored?.attemptCount shouldBe 1
            }

            test("같은 세션에 오답으로 재제출하면 200과 isOrderCorrect=false·attempt_count=2를 반환하고 post_activity_results는 여전히 1건이다") {
                val (guardianId, token) = authorizedGuardian()
                val childId = "child-${uniqueSuffix()}"
                val storyId = "story-${uniqueSuffix()}"
                val sessionId = "session-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))
                testStoryFixture.saveStory(storyEntity(storyId, postActivityConfig = testConfig))
                testSpeakingSessionFixture.save(
                    sessionEntity(sessionId, childId, storyId, status = SessionStatus.POST_ACTIVITY),
                )

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .body(mapOf("order" to correctOrderPayload))
                    .`when`()
                    .post("/speaking-sessions/$sessionId/post-activity/card-order")
                    .then()
                    .statusCode(200)
                    .body("data.isOrderCorrect", equalTo(true))
                    .body("data.attemptCount", equalTo(1))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .body(mapOf("order" to wrongOrderPayload))
                    .`when`()
                    .post("/speaking-sessions/$sessionId/post-activity/card-order")
                    .then()
                    .statusCode(200)
                    .body("data.isOrderCorrect", equalTo(false))
                    .body("data.attemptCount", equalTo(2))
                    .body("data.retellingKeywords", nullValue())

                testPostActivityResultFixture.count() shouldBe 1L
                val stored = testPostActivityResultFixture.findBySessionId(sessionId)
                stored?.isOrderCorrect shouldBe false
                stored?.attemptCount shouldBe 2
            }
        }

        context("예외케이스") {
            test("IN_PROGRESS 세션에 제출하면 422와 BUSINESS_RULE_VIOLATION을 반환하고 저장되지 않는다") {
                val (guardianId, token) = authorizedGuardian()
                val childId = "child-${uniqueSuffix()}"
                val storyId = "story-${uniqueSuffix()}"
                val sessionId = "session-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))
                testStoryFixture.saveStory(storyEntity(storyId, postActivityConfig = testConfig))
                testSpeakingSessionFixture.save(
                    sessionEntity(sessionId, childId, storyId, status = SessionStatus.IN_PROGRESS),
                )

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .body(mapOf("order" to correctOrderPayload))
                    .`when`()
                    .post("/speaking-sessions/$sessionId/post-activity/card-order")
                    .then()
                    .statusCode(422)
                    .body("success", equalTo(false))
                    .body("code", equalTo(422))
                    .body("detailCode", equalTo(ExceptionResponseCode.BUSINESS_RULE_VIOLATION.detailCode))

                testPostActivityResultFixture.count() shouldBe 0L
            }

            test("남의 아이 세션에 제출하면 404와 NOT_FOUND로 은닉한다") {
                val (_, token) = authorizedGuardian()
                val otherGuardianId = "other-${uniqueSuffix()}"
                testUserFixture.saveUser(userEntity(otherGuardianId))
                val otherChildId = "child-${uniqueSuffix()}"
                val storyId = "story-${uniqueSuffix()}"
                val sessionId = "session-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(otherChildId, otherGuardianId))
                testStoryFixture.saveStory(storyEntity(storyId, postActivityConfig = testConfig))
                testSpeakingSessionFixture.save(
                    sessionEntity(sessionId, otherChildId, storyId, status = SessionStatus.POST_ACTIVITY),
                )

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .body(mapOf("order" to correctOrderPayload))
                    .`when`()
                    .post("/speaking-sessions/$sessionId/post-activity/card-order")
                    .then()
                    .statusCode(404)
                    .body("detailCode", equalTo(ExceptionResponseCode.NOT_FOUND.detailCode))
            }

            test("존재하지 않는 세션에 제출하면 404와 NOT_FOUND를 반환한다") {
                val (_, token) = authorizedGuardian()

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .body(mapOf("order" to correctOrderPayload))
                    .`when`()
                    .post("/speaking-sessions/missing-${uniqueSuffix()}/post-activity/card-order")
                    .then()
                    .statusCode(404)
                    .body("detailCode", equalTo(ExceptionResponseCode.NOT_FOUND.detailCode))
            }

            test("토큰이 없으면 401과 EMPTY_TOKEN을 반환한다") {
                RestAssured.given()
                    .contentType(ContentType.JSON)
                    .body(mapOf("order" to correctOrderPayload))
                    .`when`()
                    .post("/speaking-sessions/any-session/post-activity/card-order")
                    .then()
                    .statusCode(401)
                    .body("detailCode", equalTo(ExceptionResponseCode.EMPTY_TOKEN.detailCode))
                    .body("message", equalTo("토큰이 없습니다."))
            }
        }
    }

    context("submitMissionAnswer") {
        val wordOrderMission = Mission(
            goal = "문장 완성하기",
            examples = listOf("남들과 다른 점을 좋은 힘으로 바꿔 보세요"),
            type = MissionType.WORD_ORDER,
            wordCards = listOf(
                WordCard(text = "남들과", correctOrder = 1),
                WordCard(text = "달라도", correctOrder = 2),
                WordCard(text = "특별한 힘이", correctOrder = 3),
                WordCard(text = "될 수 있어요", correctOrder = 4),
            ),
        )
        val correctPayload = listOf("남들과", "달라도", "특별한 힘이", "될 수 있어요")
        val wrongPayload = listOf("달라도", "남들과", "특별한 힘이", "될 수 있어요")

        context("성공") {
            test("정답 순서를 제출하면 200과 completed=true를 반환하고 mission_results 1건이 완료로 저장돼 재조회 시 인지된다") {
                val (guardianId, token) = authorizedGuardian()
                val childId = "child-${uniqueSuffix()}"
                val storyId = "story-${uniqueSuffix()}"
                val sessionId = "session-${uniqueSuffix()}"
                val sceneId = "sc-2-$storyId"
                testChildFixture.saveChild(childEntity(childId, guardianId))
                testStoryFixture.saveStory(storyEntity(storyId))
                testStoryFixture.saveScene(dialogueEntityWithMission(storyId, 2, wordOrderMission))
                testSpeakingSessionFixture.save(sessionEntity(sessionId, childId, storyId))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .body(mapOf("submittedOrder" to correctPayload))
                    .`when`()
                    .post("/speaking-sessions/$sessionId/missions/$sceneId/answer")
                    .then()
                    .statusCode(200)
                    .body("success", equalTo(true))
                    .body("data.completed", equalTo(true))
                    .body("data.attemptCount", equalTo(1))

                testMissionResultFixture.count() shouldBe 1L
                val stored = testMissionResultFixture.findBySessionIdAndSceneId(sessionId, sceneId)
                stored?.completed shouldBe true
                stored?.attemptCount shouldBe 1
            }

            test("오답 재제출 뒤 정답을 맞추면 completed=true·attempt_count=2가 되고 mission_results는 여전히 1건이다") {
                val (guardianId, token) = authorizedGuardian()
                val childId = "child-${uniqueSuffix()}"
                val storyId = "story-${uniqueSuffix()}"
                val sessionId = "session-${uniqueSuffix()}"
                val sceneId = "sc-2-$storyId"
                testChildFixture.saveChild(childEntity(childId, guardianId))
                testStoryFixture.saveStory(storyEntity(storyId))
                testStoryFixture.saveScene(dialogueEntityWithMission(storyId, 2, wordOrderMission))
                testSpeakingSessionFixture.save(sessionEntity(sessionId, childId, storyId))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .body(mapOf("submittedOrder" to wrongPayload))
                    .`when`()
                    .post("/speaking-sessions/$sessionId/missions/$sceneId/answer")
                    .then()
                    .statusCode(200)
                    .body("data.completed", equalTo(false))
                    .body("data.attemptCount", equalTo(1))
                    .body("data.hints", equalTo(wordOrderMission.examples))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .body(mapOf("submittedOrder" to correctPayload))
                    .`when`()
                    .post("/speaking-sessions/$sessionId/missions/$sceneId/answer")
                    .then()
                    .statusCode(200)
                    .body("data.completed", equalTo(true))
                    .body("data.attemptCount", equalTo(2))

                testMissionResultFixture.count() shouldBe 1L
                val stored = testMissionResultFixture.findBySessionIdAndSceneId(sessionId, sceneId)
                stored?.completed shouldBe true
                stored?.attemptCount shouldBe 2
            }
        }

        context("예외케이스") {
            test("미션이 없는 대화 장면에 제출하면 422와 BUSINESS_RULE_VIOLATION을 반환하고 저장되지 않는다") {
                val (guardianId, token) = authorizedGuardian()
                val childId = "child-${uniqueSuffix()}"
                val storyId = "story-${uniqueSuffix()}"
                val sessionId = "session-${uniqueSuffix()}"
                val sceneId = "sc-2-$storyId"
                testChildFixture.saveChild(childEntity(childId, guardianId))
                testStoryFixture.saveStory(storyEntity(storyId))
                testStoryFixture.saveScene(dialogueEntity(storyId, 2))
                testSpeakingSessionFixture.save(sessionEntity(sessionId, childId, storyId))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .body(mapOf("submittedOrder" to correctPayload))
                    .`when`()
                    .post("/speaking-sessions/$sessionId/missions/$sceneId/answer")
                    .then()
                    .statusCode(422)
                    .body("detailCode", equalTo(ExceptionResponseCode.BUSINESS_RULE_VIOLATION.detailCode))

                testMissionResultFixture.count() shouldBe 0L
            }

            test("비-DIALOGUE 신에 제출하면 422와 BUSINESS_RULE_VIOLATION을 반환하고 저장되지 않는다") {
                val (guardianId, token) = authorizedGuardian()
                val childId = "child-${uniqueSuffix()}"
                val storyId = "story-${uniqueSuffix()}"
                val sessionId = "session-${uniqueSuffix()}"
                val sceneId = "sc-1-$storyId"
                testChildFixture.saveChild(childEntity(childId, guardianId))
                testStoryFixture.saveStory(storyEntity(storyId))
                testStoryFixture.saveScene(narrationEntity(storyId, 1))
                testSpeakingSessionFixture.save(sessionEntity(sessionId, childId, storyId))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .body(mapOf("submittedOrder" to correctPayload))
                    .`when`()
                    .post("/speaking-sessions/$sessionId/missions/$sceneId/answer")
                    .then()
                    .statusCode(422)
                    .body("detailCode", equalTo(ExceptionResponseCode.BUSINESS_RULE_VIOLATION.detailCode))

                testMissionResultFixture.count() shouldBe 0L
            }

            test("남의 아이 세션에 제출하면 404와 NOT_FOUND로 은닉하고 저장되지 않는다") {
                val (_, token) = authorizedGuardian()
                val otherGuardianId = "other-${uniqueSuffix()}"
                testUserFixture.saveUser(userEntity(otherGuardianId))
                val otherChildId = "child-${uniqueSuffix()}"
                val storyId = "story-${uniqueSuffix()}"
                val sessionId = "session-${uniqueSuffix()}"
                val sceneId = "sc-2-$storyId"
                testChildFixture.saveChild(childEntity(otherChildId, otherGuardianId))
                testStoryFixture.saveStory(storyEntity(storyId))
                testStoryFixture.saveScene(dialogueEntityWithMission(storyId, 2, wordOrderMission))
                testSpeakingSessionFixture.save(sessionEntity(sessionId, otherChildId, storyId))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .body(mapOf("submittedOrder" to correctPayload))
                    .`when`()
                    .post("/speaking-sessions/$sessionId/missions/$sceneId/answer")
                    .then()
                    .statusCode(404)
                    .body("detailCode", equalTo(ExceptionResponseCode.NOT_FOUND.detailCode))

                testMissionResultFixture.count() shouldBe 0L
            }
        }
    }

    context("submitRetelling") {
        val cardA = PostActivityConfig.Card(id = "card-a", text = "A 장면", correctOrder = 1)
        val cardB = PostActivityConfig.Card(id = "card-b", text = "B 장면", correctOrder = 2)
        val cardC = PostActivityConfig.Card(id = "card-c", text = "C 장면", correctOrder = 3)
        val retellingKeywords = listOf("방귀", "며느리", "시아버지")
        val testConfig = PostActivityConfig(
            cards = listOf(cardC, cardA, cardB),
            retellingKeywords = retellingKeywords,
        )
        val correctOrderPayload = listOf("card-a", "card-b", "card-c")
        val wrongOrderPayload = listOf("card-c", "card-b", "card-a")
        val validText = "방귀쟁이 며느리는 시아버지 덕분에 방귀를 뀔 수 있었어요"

        fun requestPart(text: String): MultiPartSpecification =
            MultiPartSpecBuilder("""{"text":"$text"}""".toByteArray(StandardCharsets.UTF_8))
                .controlName("request")
                .mimeType("application/json")
                .charset(StandardCharsets.UTF_8)
                .build()

        context("성공") {
            test("POST_ACTIVITY 세션에서 카드 정답 제출 후 재구성 음성과 함께 제출하면 200·retellingAudioUrl non-null·세션 COMPLETED를 반환하고 URL이 저장된다") {
                val (guardianId, token) = authorizedGuardian()
                val childId = "child-${uniqueSuffix()}"
                val storyId = "story-${uniqueSuffix()}"
                val sessionId = "session-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))
                testStoryFixture.saveStory(storyEntity(storyId, postActivityConfig = testConfig))
                testSpeakingSessionFixture.save(
                    sessionEntity(sessionId, childId, storyId, status = SessionStatus.POST_ACTIVITY),
                )

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .body(mapOf("order" to correctOrderPayload))
                    .`when`()
                    .post("/speaking-sessions/$sessionId/post-activity/card-order")
                    .then()
                    .statusCode(200)
                    .body("data.isOrderCorrect", equalTo(true))

                val storedAudioUrl = RestAssured.given()
                    .header("Authorization", token)
                    .multiPart(requestPart(validText))
                    .multiPart("audio", "retelling.m4a", "audio-bytes".toByteArray(), "audio/mp4")
                    .`when`()
                    .post("/speaking-sessions/$sessionId/post-activity/retelling")
                    .then()
                    .statusCode(200)
                    .body("success", equalTo(true))
                    .body("code", equalTo(200))
                    .body("data.retellingText", equalTo(validText))
                    .body("data.retellingAudioUrl", not(emptyOrNullString()))
                    .body("data.completedAt", not(emptyOrNullString()))
                    .body("data.status", equalTo(SessionStatus.COMPLETED.name))
                    .extract()
                    .path<String>("data.retellingAudioUrl")

                storedAudioUrl.startsWith("/files/") shouldBe true

                val storedResult = testPostActivityResultFixture.findBySessionId(sessionId)
                storedResult?.retellingText shouldBe validText
                storedResult?.retellingAudioUrl shouldBe storedAudioUrl
                storedResult?.completedAt shouldNotBe null

                val storedSession = testSpeakingSessionFixture.findBySessionId(sessionId)
                storedSession?.status shouldBe SessionStatus.COMPLETED.name
            }

            test("음성 없이 재구성 텍스트만 제출하면 200·retellingAudioUrl null·세션 COMPLETED를 반환하고 URL이 null로 저장된다") {
                val (guardianId, token) = authorizedGuardian()
                val childId = "child-${uniqueSuffix()}"
                val storyId = "story-${uniqueSuffix()}"
                val sessionId = "session-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))
                testStoryFixture.saveStory(storyEntity(storyId, postActivityConfig = testConfig))
                testSpeakingSessionFixture.save(
                    sessionEntity(sessionId, childId, storyId, status = SessionStatus.POST_ACTIVITY),
                )

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .body(mapOf("order" to correctOrderPayload))
                    .`when`()
                    .post("/speaking-sessions/$sessionId/post-activity/card-order")
                    .then()
                    .statusCode(200)
                    .body("data.isOrderCorrect", equalTo(true))

                RestAssured.given()
                    .header("Authorization", token)
                    .multiPart(requestPart(validText))
                    .`when`()
                    .post("/speaking-sessions/$sessionId/post-activity/retelling")
                    .then()
                    .statusCode(200)
                    .body("data.retellingText", equalTo(validText))
                    .body("data.retellingAudioUrl", nullValue())
                    .body("data.completedAt", not(emptyOrNullString()))
                    .body("data.status", equalTo(SessionStatus.COMPLETED.name))

                val storedResult = testPostActivityResultFixture.findBySessionId(sessionId)
                storedResult?.retellingText shouldBe validText
                storedResult?.retellingAudioUrl shouldBe null
                storedResult?.completedAt shouldNotBe null

                val storedSession = testSpeakingSessionFixture.findBySessionId(sessionId)
                storedSession?.status shouldBe SessionStatus.COMPLETED.name
            }
        }

        context("예외케이스") {
            test("카드 순서 결과 없이 재구성 제출하면 422와 BUSINESS_RULE_VIOLATION을 반환하고 저장되지 않는다") {
                val (guardianId, token) = authorizedGuardian()
                val childId = "child-${uniqueSuffix()}"
                val storyId = "story-${uniqueSuffix()}"
                val sessionId = "session-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))
                testStoryFixture.saveStory(storyEntity(storyId, postActivityConfig = testConfig))
                testSpeakingSessionFixture.save(
                    sessionEntity(sessionId, childId, storyId, status = SessionStatus.POST_ACTIVITY),
                )

                RestAssured.given()
                    .header("Authorization", token)
                    .multiPart(requestPart(validText))
                    .`when`()
                    .post("/speaking-sessions/$sessionId/post-activity/retelling")
                    .then()
                    .statusCode(422)
                    .body("success", equalTo(false))
                    .body("code", equalTo(422))
                    .body("detailCode", equalTo(ExceptionResponseCode.BUSINESS_RULE_VIOLATION.detailCode))

                val storedSession = testSpeakingSessionFixture.findBySessionId(sessionId)
                storedSession?.status shouldBe SessionStatus.POST_ACTIVITY.name
            }

            test("카드 오답 후 재구성 제출하면 422와 BUSINESS_RULE_VIOLATION을 반환하고 세션이 완료되지 않는다") {
                val (guardianId, token) = authorizedGuardian()
                val childId = "child-${uniqueSuffix()}"
                val storyId = "story-${uniqueSuffix()}"
                val sessionId = "session-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))
                testStoryFixture.saveStory(storyEntity(storyId, postActivityConfig = testConfig))
                testSpeakingSessionFixture.save(
                    sessionEntity(sessionId, childId, storyId, status = SessionStatus.POST_ACTIVITY),
                )

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .body(mapOf("order" to wrongOrderPayload))
                    .`when`()
                    .post("/speaking-sessions/$sessionId/post-activity/card-order")
                    .then()
                    .statusCode(200)
                    .body("data.isOrderCorrect", equalTo(false))

                RestAssured.given()
                    .header("Authorization", token)
                    .multiPart(requestPart(validText))
                    .`when`()
                    .post("/speaking-sessions/$sessionId/post-activity/retelling")
                    .then()
                    .statusCode(422)
                    .body("detailCode", equalTo(ExceptionResponseCode.BUSINESS_RULE_VIOLATION.detailCode))

                val storedSession = testSpeakingSessionFixture.findBySessionId(sessionId)
                storedSession?.status shouldBe SessionStatus.POST_ACTIVITY.name
            }

            test("공백 재구성 발화 텍스트는 400과 INVALID_DTO_PARAMETER를 반환하고 완료되지 않는다") {
                val (guardianId, token) = authorizedGuardian()
                val childId = "child-${uniqueSuffix()}"
                val storyId = "story-${uniqueSuffix()}"
                val sessionId = "session-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))
                testStoryFixture.saveStory(storyEntity(storyId, postActivityConfig = testConfig))
                testSpeakingSessionFixture.save(
                    sessionEntity(sessionId, childId, storyId, status = SessionStatus.POST_ACTIVITY),
                )

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .body(mapOf("order" to correctOrderPayload))
                    .`when`()
                    .post("/speaking-sessions/$sessionId/post-activity/card-order")
                    .then()
                    .statusCode(200)
                    .body("data.isOrderCorrect", equalTo(true))

                RestAssured.given()
                    .header("Authorization", token)
                    .multiPart(requestPart("   "))
                    .`when`()
                    .post("/speaking-sessions/$sessionId/post-activity/retelling")
                    .then()
                    .statusCode(400)
                    .body("success", equalTo(false))
                    .body("code", equalTo(400))
                    .body("detailCode", equalTo(ExceptionResponseCode.INVALID_DTO_PARAMETER.detailCode))

                val storedResult = testPostActivityResultFixture.findBySessionId(sessionId)
                storedResult?.retellingText shouldBe null
                storedResult?.retellingAudioUrl shouldBe null
                storedResult?.completedAt shouldBe null

                val storedSession = testSpeakingSessionFixture.findBySessionId(sessionId)
                storedSession?.status shouldBe SessionStatus.POST_ACTIVITY.name
            }

            test("남의 아이 세션에 재구성 제출하면 404와 NOT_FOUND로 은닉한다") {
                val (_, token) = authorizedGuardian()
                val otherGuardianId = "other-${uniqueSuffix()}"
                testUserFixture.saveUser(userEntity(otherGuardianId))
                val otherChildId = "child-${uniqueSuffix()}"
                val storyId = "story-${uniqueSuffix()}"
                val sessionId = "session-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(otherChildId, otherGuardianId))
                testStoryFixture.saveStory(storyEntity(storyId, postActivityConfig = testConfig))
                testSpeakingSessionFixture.save(
                    sessionEntity(sessionId, otherChildId, storyId, status = SessionStatus.POST_ACTIVITY),
                )

                RestAssured.given()
                    .header("Authorization", token)
                    .multiPart(requestPart(validText))
                    .`when`()
                    .post("/speaking-sessions/$sessionId/post-activity/retelling")
                    .then()
                    .statusCode(404)
                    .body("detailCode", equalTo(ExceptionResponseCode.NOT_FOUND.detailCode))
            }

            test("존재하지 않는 세션에 재구성 제출하면 404와 NOT_FOUND를 반환한다") {
                val (_, token) = authorizedGuardian()

                RestAssured.given()
                    .header("Authorization", token)
                    .multiPart(requestPart(validText))
                    .`when`()
                    .post("/speaking-sessions/missing-${uniqueSuffix()}/post-activity/retelling")
                    .then()
                    .statusCode(404)
                    .body("detailCode", equalTo(ExceptionResponseCode.NOT_FOUND.detailCode))
            }

            test("토큰이 없으면 401과 EMPTY_TOKEN을 반환한다") {
                RestAssured.given()
                    .multiPart(requestPart(validText))
                    .`when`()
                    .post("/speaking-sessions/any-session/post-activity/retelling")
                    .then()
                    .statusCode(401)
                    .body("detailCode", equalTo(ExceptionResponseCode.EMPTY_TOKEN.detailCode))
                    .body("message", equalTo("토큰이 없습니다."))
            }
        }
    }

    context("getSessionReport") {
        fun childMessage(
            sessionId: String,
            sceneId: String,
            messageId: String,
            turnOrder: Long,
            audioUrl: String? = null,
        ): MessageOrmEntity =
            MessageOrmEntity(
                id = messageId,
                sessionId = sessionId,
                sceneId = sceneId,
                speakerType = "CHILD",
                turnOrder = turnOrder,
                text = "아이 발화 $turnOrder",
                sttRawText = "아이 발화 $turnOrder",
                audioUrl = audioUrl,
                createdAt = LocalDateTime.now().minusMinutes(10 - turnOrder),
            )

        fun characterMessage(sessionId: String, sceneId: String, messageId: String, turnOrder: Long): MessageOrmEntity =
            MessageOrmEntity(
                id = messageId,
                sessionId = sessionId,
                sceneId = sceneId,
                speakerType = "CHARACTER",
                turnOrder = turnOrder,
                text = "캐릭터 응답 $turnOrder",
                sttRawText = null,
                createdAt = LocalDateTime.now().minusMinutes(10 - turnOrder),
            )

        fun analysisEntity(messageId: String, vararg types: ThinkingElement): UtteranceAnalysisOrmEntity =
            UtteranceAnalysisOrmEntity(
                id = "analysis-$messageId",
                messageId = messageId,
                childIntent = ChildIntent.OPINION.name,
                mainPoint = "핵심 $messageId",
                detectedElements = types.map { DetectedElement(type = it, evidence = "근거-${it.name}") },
                utteranceValidity = UtteranceValidity.VALID.name,
            )

        fun dialogueEntityWithTitle(storyId: String, sceneOrder: Short, title: String): SceneOrmEntity =
            SceneOrmEntity(
                sceneId = "sc-$sceneOrder-$storyId",
                storyId = storyId,
                sceneOrder = sceneOrder,
                chapter = 1,
                sceneType = SceneType.DIALOGUE.name,
                sceneDescription = "대화 설명 $sceneOrder",
                title = title,
                characterName = "ch_banggui_daughter_in_law",
                characterDisplayName = "방귀쟁이 며느리",
                characterOpening = null,
                characterClosing = null,
                conflict = null,
                sceneGoal = "며느리의 입장을 이해하고 공감해준다",
                requiredElements = listOf(ThinkingElement.PERSPECTIVE, ThinkingElement.EMOTION),
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

        fun completedPostActivity(sessionId: String): PostActivityResultOrmEntity = PostActivityResultOrmEntity(
            id = "post-$sessionId",
            sessionId = sessionId,
            submittedOrder = listOf("card_1", "card_2"),
            isOrderCorrect = true,
            attemptCount = 1,
            retellingText = "며느리가 방귀로 마을 사람들을 도왔어요",
            completedAt = LocalDateTime.now(),
        )

        context("성공") {
            test("완료 세션 GET report는 200과 5개 탭을 반환하고 장면 카드가 마지막 아이 발화·직전 캐릭터 질문·오디오를 담는다") {
                val (guardianId, token) = authorizedGuardian()
                val childId = "child-${uniqueSuffix()}"
                val storyId = "story-${uniqueSuffix()}"
                val sessionId = "session-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))
                testStoryFixture.saveStory(storyEntity(storyId))
                testStoryFixture.saveScene(dialogueEntityWithTitle(storyId, 1, "걱정하는 며느리"))
                testStoryFixture.saveScene(dialogueEntityWithTitle(storyId, 3, "화가 난 시아버지"))
                testSpeakingSessionFixture.save(
                    sessionEntity(sessionId, childId, storyId, status = SessionStatus.COMPLETED),
                )
                val firstSceneId = "sc-1-$storyId"
                val secondSceneId = "sc-3-$storyId"
                val audioUrl = "/files/utterance-msg-child-2.webm"
                testMessageFixture.save(childMessage(sessionId, firstSceneId, "msg-child-1", 1))
                testMessageFixture.save(characterMessage(sessionId, firstSceneId, "msg-char-1", 2))
                testMessageFixture.save(childMessage(sessionId, firstSceneId, "msg-child-2", 3, audioUrl = audioUrl))
                testMessageFixture.save(childMessage(sessionId, secondSceneId, "msg-child-3", 4))
                testUtteranceAnalysisFixture.save(
                    analysisEntity("msg-child-1", ThinkingElement.EMOTION, ThinkingElement.PERSPECTIVE),
                )
                testUtteranceAnalysisFixture.save(
                    analysisEntity("msg-child-2", ThinkingElement.EMOTION, ThinkingElement.DECISION),
                )
                testPostActivityResultFixture.save(completedPostActivity(sessionId))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .`when`()
                    .get("/speaking-sessions/$sessionId/report")
                    .then()
                    .statusCode(200)
                    .body("success", equalTo(true))
                    .body("code", equalTo(200))
                    .body("message", equalTo(SuccessResponseCode.OK.message))
                    .body("data.summaryTab.storyTitle", equalTo("제목-$storyId"))
                    .body("data.summaryTab.durationMinutes", equalTo(4))
                    .body("data.summaryTab.postActivityCompleted.cardOrder", equalTo(true))
                    .body("data.summaryTab.postActivityCompleted.retelling", equalTo(true))
                    .body("data.summaryTab.overall.headline", not(emptyOrNullString()))
                    .body("data.summaryTab.participation.size()", equalTo(3))
                    .body("data.speechTab.areas.size()", equalTo(3))
                    .body("data.sceneTab.cards.size()", equalTo(2))
                    .body("data.sceneTab.cards[0].sceneId", equalTo(firstSceneId))
                    .body("data.sceneTab.cards[0].title", equalTo("걱정하는 며느리"))
                    .body("data.sceneTab.cards[0].characterQuestion", equalTo("캐릭터 응답 2"))
                    .body("data.sceneTab.cards[0].childUtterance.text", equalTo("아이 발화 3"))
                    .body("data.sceneTab.cards[0].childUtterance.audioUrl", equalTo(audioUrl))
                    .body("data.sceneTab.cards[1].sceneId", equalTo(secondSceneId))
                    .body("data.sceneTab.cards[1].title", equalTo("화가 난 시아버지"))
                    .body("data.sceneTab.cards[1].childUtterance.text", equalTo("아이 발화 4"))
                    .body("data.sceneTab.cards[1].childUtterance.audioUrl", nullValue())
                    .body("data.representativeTab.text", not(emptyOrNullString()))
                    .body("data.homeGuideTab.storyQuestions.size()", equalTo(3))
                    .body("data.homeGuideTab.dailyQuestions.size()", equalTo(3))
                    .body("data.createdAt", not(emptyOrNullString()))

                testReportFixture.count() shouldBe 1L
                val stored = testReportFixture.findBySessionId(sessionId).shouldNotBeNull()
                stored.overall.shouldNotBeNull()
                stored.participation.shouldNotBeNull()
                stored.speechAnalyses.shouldNotBeNull()
                stored.representativeUtterance.shouldNotBeNull()
                stored.homeGuide.shouldNotBeNull()
                val storedHighlights = stored.sceneHighlights.shouldNotBeNull()
                storedHighlights.map { it.sceneId to it.messageId } shouldBe
                    listOf(firstSceneId to "msg-child-2", secondSceneId to "msg-child-3")
            }

            test("같은 완료 세션에 재요청하면 200과 동일 리포트를 반환하고 reports는 여전히 1건이다") {
                val (guardianId, token) = authorizedGuardian()
                val childId = "child-${uniqueSuffix()}"
                val storyId = "story-${uniqueSuffix()}"
                val sessionId = "session-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))
                testStoryFixture.saveStory(storyEntity(storyId))
                testStoryFixture.saveScene(dialogueEntity(storyId, 1))
                testSpeakingSessionFixture.save(
                    sessionEntity(sessionId, childId, storyId, status = SessionStatus.COMPLETED),
                )
                val sceneId = "sc-1-$storyId"
                testMessageFixture.save(childMessage(sessionId, sceneId, "msg-child-1", 1))
                testUtteranceAnalysisFixture.save(analysisEntity("msg-child-1", ThinkingElement.EMOTION))

                val firstCreatedAt = RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .`when`()
                    .get("/speaking-sessions/$sessionId/report")
                    .then()
                    .statusCode(200)
                    .body("data.summaryTab.storyTitle", not(emptyOrNullString()))
                    .extract()
                    .path<String>("data.createdAt")

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .`when`()
                    .get("/speaking-sessions/$sessionId/report")
                    .then()
                    .statusCode(200)
                    .body("data.createdAt", equalTo(firstCreatedAt))

                testReportFixture.count() shouldBe 1L
            }

            test("옛 구조 저장 행(새 섹션 컬럼 null)은 첫 조회 때 같은 행 하나에 새 구조로 다시 생성된다") {
                val (guardianId, token) = authorizedGuardian()
                val childId = "child-${uniqueSuffix()}"
                val storyId = "story-${uniqueSuffix()}"
                val sessionId = "session-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))
                testStoryFixture.saveStory(storyEntity(storyId))
                testStoryFixture.saveScene(dialogueEntity(storyId, 1))
                testSpeakingSessionFixture.save(
                    sessionEntity(sessionId, childId, storyId, status = SessionStatus.COMPLETED),
                )
                val sceneId = "sc-1-$storyId"
                testMessageFixture.save(childMessage(sessionId, sceneId, "msg-child-1", 1))
                testMessageFixture.save(childMessage(sessionId, sceneId, "msg-child-2", 2))
                testUtteranceAnalysisFixture.save(analysisEntity("msg-child-1", ThinkingElement.EMOTION))
                val legacyRow = testReportFixture.saveLegacyStructureRow(sessionId)

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .`when`()
                    .get("/speaking-sessions/$sessionId/report")
                    .then()
                    .statusCode(200)
                    .body("data.summaryTab.storyTitle", not(emptyOrNullString()))

                testReportFixture.count() shouldBe 1L
                val stored = testReportFixture.findBySessionId(sessionId).shouldNotBeNull()
                stored.id shouldBe legacyRow.id
                stored.overall.shouldNotBeNull()
                stored.participation.shouldNotBeNull()
                stored.speechAnalyses.shouldNotBeNull()
                stored.representativeUtterance.shouldNotBeNull()
                stored.homeGuide.shouldNotBeNull()
                val storedHighlights = stored.sceneHighlights.shouldNotBeNull()
                storedHighlights.map { it.messageId } shouldBe listOf("msg-child-2")
            }
        }

        context("예외케이스") {
            test("IN_PROGRESS 세션 report는 422와 BUSINESS_RULE_VIOLATION을 반환하고 저장되지 않는다") {
                val (guardianId, token) = authorizedGuardian()
                val childId = "child-${uniqueSuffix()}"
                val storyId = "story-${uniqueSuffix()}"
                val sessionId = "session-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))
                testStoryFixture.saveStory(storyEntity(storyId))
                testSpeakingSessionFixture.save(
                    sessionEntity(sessionId, childId, storyId, status = SessionStatus.IN_PROGRESS),
                )

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .`when`()
                    .get("/speaking-sessions/$sessionId/report")
                    .then()
                    .statusCode(422)
                    .body("success", equalTo(false))
                    .body("code", equalTo(422))
                    .body("detailCode", equalTo(ExceptionResponseCode.BUSINESS_RULE_VIOLATION.detailCode))

                testReportFixture.count() shouldBe 0L
            }

            test("남의 아이 완료 세션 report는 404와 NOT_FOUND로 은닉한다") {
                val (_, token) = authorizedGuardian()
                val otherGuardianId = "other-${uniqueSuffix()}"
                testUserFixture.saveUser(userEntity(otherGuardianId))
                val otherChildId = "child-${uniqueSuffix()}"
                val storyId = "story-${uniqueSuffix()}"
                val sessionId = "session-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(otherChildId, otherGuardianId))
                testStoryFixture.saveStory(storyEntity(storyId))
                testSpeakingSessionFixture.save(
                    sessionEntity(sessionId, otherChildId, storyId, status = SessionStatus.COMPLETED),
                )

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .`when`()
                    .get("/speaking-sessions/$sessionId/report")
                    .then()
                    .statusCode(404)
                    .body("detailCode", equalTo(ExceptionResponseCode.NOT_FOUND.detailCode))

                testReportFixture.count() shouldBe 0L
            }

            test("존재하지 않는 세션 report는 404와 NOT_FOUND를 반환한다") {
                val (_, token) = authorizedGuardian()

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .`when`()
                    .get("/speaking-sessions/missing-${uniqueSuffix()}/report")
                    .then()
                    .statusCode(404)
                    .body("detailCode", equalTo(ExceptionResponseCode.NOT_FOUND.detailCode))
            }

            test("토큰이 없으면 401과 EMPTY_TOKEN을 반환한다") {
                RestAssured.given()
                    .contentType(ContentType.JSON)
                    .`when`()
                    .get("/speaking-sessions/any-session/report")
                    .then()
                    .statusCode(401)
                    .body("detailCode", equalTo(ExceptionResponseCode.EMPTY_TOKEN.detailCode))
                    .body("message", equalTo("토큰이 없습니다."))
            }
        }
    }

    context("deleteSpeakingSession") {
        context("성공") {
            test("본인 아이 세션을 폐기하면 204를 반환하고 세션이 DB에서 사라진다") {
                val (guardianId, token) = authorizedGuardian()
                val childId = "child-${uniqueSuffix()}"
                val storyId = "story-${uniqueSuffix()}"
                val sessionId = "session-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))
                testStoryFixture.saveStory(storyEntity(storyId))
                testStoryFixture.saveScene(narrationEntity(storyId, 1))
                testSpeakingSessionFixture.save(
                    sessionEntity(sessionId, childId, storyId, currentSceneId = "sc-1-$storyId"),
                )

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .`when`()
                    .delete("/speaking-sessions/$sessionId")
                    .then()
                    .statusCode(204)

                testSpeakingSessionFixture.findBySessionId(sessionId) shouldBe null
            }

            test("완료(COMPLETED)된 세션도 폐기할 수 있다") {
                val (guardianId, token) = authorizedGuardian()
                val childId = "child-${uniqueSuffix()}"
                val storyId = "story-${uniqueSuffix()}"
                val sessionId = "session-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))
                testStoryFixture.saveStory(storyEntity(storyId))
                testStoryFixture.saveScene(narrationEntity(storyId, 1))
                testSpeakingSessionFixture.save(
                    sessionEntity(sessionId, childId, storyId, status = SessionStatus.COMPLETED),
                )

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .`when`()
                    .delete("/speaking-sessions/$sessionId")
                    .then()
                    .statusCode(204)

                testSpeakingSessionFixture.findBySessionId(sessionId) shouldBe null
            }
        }
        context("예외케이스") {
            test("다른 보호자의 아이 세션이면 404로 은닉하고 세션은 남는다") {
                val (_, intruderToken) = authorizedGuardian()
                val (ownerGuardianId, _) = authorizedGuardian()
                val childId = "child-${uniqueSuffix()}"
                val storyId = "story-${uniqueSuffix()}"
                val sessionId = "session-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, ownerGuardianId))
                testStoryFixture.saveStory(storyEntity(storyId))
                testSpeakingSessionFixture.save(sessionEntity(sessionId, childId, storyId))

                RestAssured.given()
                    .header("Authorization", intruderToken)
                    .contentType(ContentType.JSON)
                    .`when`()
                    .delete("/speaking-sessions/$sessionId")
                    .then()
                    .statusCode(404)
                    .body("detailCode", equalTo(ExceptionResponseCode.NOT_FOUND.detailCode))

                testSpeakingSessionFixture.findBySessionId(sessionId)?.sessionId shouldBe sessionId
            }

            test("없는 세션이면 404를 반환한다") {
                val (_, token) = authorizedGuardian()

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .`when`()
                    .delete("/speaking-sessions/none-${uniqueSuffix()}")
                    .then()
                    .statusCode(404)
                    .body("detailCode", equalTo(ExceptionResponseCode.NOT_FOUND.detailCode))
            }
        }
    }

    context("stopSpeakingSession") {
        context("성공") {
            test("IN_PROGRESS 세션 stop은 200과 status=IN_PROGRESS를 반환하고 저장 상태도 IN_PROGRESS로 유지된다") {
                val (guardianId, token) = authorizedGuardian()
                val childId = "child-${uniqueSuffix()}"
                val storyId = "story-${uniqueSuffix()}"
                val sessionId = "session-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))
                testStoryFixture.saveStory(storyEntity(storyId))
                testStoryFixture.saveScene(narrationEntity(storyId, 1))
                testSpeakingSessionFixture.save(
                    sessionEntity(sessionId, childId, storyId, currentSceneId = "sc-1-$storyId"),
                )

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .`when`()
                    .post("/speaking-sessions/$sessionId/stop")
                    .then()
                    .statusCode(200)
                    .body("success", equalTo(true))
                    .body("code", equalTo(200))
                    .body("message", equalTo(SuccessResponseCode.OK.message))
                    .body("data.sessionId", equalTo(sessionId))
                    .body("data.status", equalTo(SessionStatus.IN_PROGRESS.name))

                val storedSession = testSpeakingSessionFixture.findBySessionId(sessionId)
                storedSession?.status shouldBe SessionStatus.IN_PROGRESS.name
            }

            test("POST_ACTIVITY 세션 stop은 200과 status=POST_ACTIVITY를 반환하고 저장 상태도 POST_ACTIVITY로 유지된다") {
                val (guardianId, token) = authorizedGuardian()
                val childId = "child-${uniqueSuffix()}"
                val storyId = "story-${uniqueSuffix()}"
                val sessionId = "session-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))
                testStoryFixture.saveStory(storyEntity(storyId))
                testStoryFixture.saveScene(narrationEntity(storyId, 1))
                testSpeakingSessionFixture.save(
                    sessionEntity(
                        sessionId,
                        childId,
                        storyId,
                        currentSceneId = "sc-1-$storyId",
                        status = SessionStatus.POST_ACTIVITY,
                    ),
                )

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .`when`()
                    .post("/speaking-sessions/$sessionId/stop")
                    .then()
                    .statusCode(200)
                    .body("data.sessionId", equalTo(sessionId))
                    .body("data.status", equalTo(SessionStatus.POST_ACTIVITY.name))

                val storedSession = testSpeakingSessionFixture.findBySessionId(sessionId)
                storedSession?.status shouldBe SessionStatus.POST_ACTIVITY.name
            }
        }

        context("예외케이스") {
            test("이미 COMPLETED 세션 stop은 422와 BUSINESS_RULE_VIOLATION을 반환하고 status가 불변이다") {
                val (guardianId, token) = authorizedGuardian()
                val childId = "child-${uniqueSuffix()}"
                val storyId = "story-${uniqueSuffix()}"
                val sessionId = "session-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))
                testStoryFixture.saveStory(storyEntity(storyId))
                testSpeakingSessionFixture.save(
                    sessionEntity(sessionId, childId, storyId, status = SessionStatus.COMPLETED),
                )

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .`when`()
                    .post("/speaking-sessions/$sessionId/stop")
                    .then()
                    .statusCode(422)
                    .body("success", equalTo(false))
                    .body("code", equalTo(422))
                    .body("detailCode", equalTo(ExceptionResponseCode.BUSINESS_RULE_VIOLATION.detailCode))

                val storedSession = testSpeakingSessionFixture.findBySessionId(sessionId)
                storedSession?.status shouldBe SessionStatus.COMPLETED.name
            }

            test("남의 아이 세션 stop은 404와 NOT_FOUND로 은닉한다") {
                val (_, token) = authorizedGuardian()
                val otherGuardianId = "other-${uniqueSuffix()}"
                testUserFixture.saveUser(userEntity(otherGuardianId))
                val otherChildId = "child-${uniqueSuffix()}"
                val storyId = "story-${uniqueSuffix()}"
                val sessionId = "session-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(otherChildId, otherGuardianId))
                testStoryFixture.saveStory(storyEntity(storyId))
                testSpeakingSessionFixture.save(sessionEntity(sessionId, otherChildId, storyId, currentSceneId = null))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .`when`()
                    .post("/speaking-sessions/$sessionId/stop")
                    .then()
                    .statusCode(404)
                    .body("detailCode", equalTo(ExceptionResponseCode.NOT_FOUND.detailCode))
            }

            test("토큰이 없으면 stop은 401과 EMPTY_TOKEN을 반환한다") {
                RestAssured.given()
                    .contentType(ContentType.JSON)
                    .`when`()
                    .post("/speaking-sessions/any-session/stop")
                    .then()
                    .statusCode(401)
                    .body("detailCode", equalTo(ExceptionResponseCode.EMPTY_TOKEN.detailCode))
                    .body("message", equalTo("토큰이 없습니다."))
            }
        }
    }

    context("goBackSpeakingScene") {
        context("성공") {
            test("화자 단위로 구성된 두 번째 챕터 세션 back은 200과 이전 챕터 첫 장면 SCENE 뷰를 반환하고 진행 상태를 0으로 초기화한다") {
                val (guardianId, token) = authorizedGuardian()
                val childId = "child-${uniqueSuffix()}"
                val storyId = "story-${uniqueSuffix()}"
                val sessionId = "session-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))
                testStoryFixture.saveStory(storyEntity(storyId))
                testStoryFixture.saveScene(narrationEntity(storyId, 1, chapter = 1))
                testStoryFixture.saveScene(characterLineEntity(storyId, 2, chapter = 1))
                testStoryFixture.saveScene(dialogueEntity(storyId, 3, chapter = 1))
                testStoryFixture.saveScene(characterLineEntity(storyId, 4, chapter = 1))
                testStoryFixture.saveScene(narrationEntity(storyId, 5, chapter = 2))
                testStoryFixture.saveScene(characterLineEntity(storyId, 6, chapter = 2))
                testStoryFixture.saveScene(dialogueEntity(storyId, 7, chapter = 2))
                val previousChapterFirstSceneId = "sc-1-$storyId"
                val currentSceneId = "sc-7-$storyId"
                testSpeakingSessionFixture.save(
                    sessionEntity(
                        sessionId,
                        childId,
                        storyId,
                        currentSceneId = currentSceneId,
                        accumulatedElements = listOf(ThinkingElement.EMOTION, ThinkingElement.PERSPECTIVE),
                        currentChildTurnCount = 3,
                        turnsWithoutNewElement = 2,
                        sceneEndReason = SceneEndReason.MAX_TURNS.name,
                    ),
                )

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .`when`()
                    .post("/speaking-sessions/$sessionId/back")
                    .then()
                    .statusCode(200)
                    .body("success", equalTo(true))
                    .body("code", equalTo(200))
                    .body("data.viewType", equalTo("SCENE"))
                    .body("data.intro", nullValue())
                    .body("data.scene.sceneId", equalTo(previousChapterFirstSceneId))
                    .body("data.scene.sceneOrder", equalTo(1))
                    .body("data.scene.chapter", equalTo(1))
                    .body("data.scene.sceneType", equalTo(SceneType.NARRATION.name))

                val storedSession = testSpeakingSessionFixture.findBySessionId(sessionId)
                storedSession?.currentSceneId shouldBe previousChapterFirstSceneId
                storedSession?.accumulatedElements shouldBe emptyList()
                storedSession?.currentChildTurnCount shouldBe 0
                storedSession?.turnsWithoutNewElement shouldBe 0
                storedSession?.sceneEndReason shouldBe null
                storedSession?.status shouldBe SessionStatus.IN_PROGRESS.name
            }
        }

        context("예외케이스") {
            test("첫 챕터 장면 세션 back은 422와 BUSINESS_RULE_VIOLATION을 반환하고 status가 불변이다") {
                val (guardianId, token) = authorizedGuardian()
                val childId = "child-${uniqueSuffix()}"
                val storyId = "story-${uniqueSuffix()}"
                val sessionId = "session-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))
                testStoryFixture.saveStory(storyEntity(storyId))
                testStoryFixture.saveScene(narrationEntity(storyId, 1, chapter = 1))
                testStoryFixture.saveScene(dialogueEntity(storyId, 3, chapter = 1))
                val firstSceneId = "sc-1-$storyId"
                testSpeakingSessionFixture.save(
                    sessionEntity(sessionId, childId, storyId, currentSceneId = firstSceneId),
                )

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .`when`()
                    .post("/speaking-sessions/$sessionId/back")
                    .then()
                    .statusCode(422)
                    .body("success", equalTo(false))
                    .body("code", equalTo(422))
                    .body("detailCode", equalTo(ExceptionResponseCode.BUSINESS_RULE_VIOLATION.detailCode))

                val storedSession = testSpeakingSessionFixture.findBySessionId(sessionId)
                storedSession?.currentSceneId shouldBe firstSceneId
                storedSession?.status shouldBe SessionStatus.IN_PROGRESS.name
            }

            test("도입 상태 세션 back은 422와 BUSINESS_RULE_VIOLATION을 반환한다") {
                val (guardianId, token) = authorizedGuardian()
                val childId = "child-${uniqueSuffix()}"
                val storyId = "story-${uniqueSuffix()}"
                val sessionId = "session-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))
                testStoryFixture.saveStory(storyEntity(storyId))
                testStoryFixture.saveScene(narrationEntity(storyId, 1))
                testSpeakingSessionFixture.save(sessionEntity(sessionId, childId, storyId, currentSceneId = null))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .`when`()
                    .post("/speaking-sessions/$sessionId/back")
                    .then()
                    .statusCode(422)
                    .body("detailCode", equalTo(ExceptionResponseCode.BUSINESS_RULE_VIOLATION.detailCode))
            }

            test("남의 아이 세션 back은 404와 NOT_FOUND로 은닉한다") {
                val (_, token) = authorizedGuardian()
                val otherGuardianId = "other-${uniqueSuffix()}"
                testUserFixture.saveUser(userEntity(otherGuardianId))
                val otherChildId = "child-${uniqueSuffix()}"
                val storyId = "story-${uniqueSuffix()}"
                val sessionId = "session-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(otherChildId, otherGuardianId))
                testStoryFixture.saveStory(storyEntity(storyId))
                testStoryFixture.saveScene(narrationEntity(storyId, 1))
                testStoryFixture.saveScene(dialogueEntity(storyId, 3))
                testSpeakingSessionFixture.save(
                    sessionEntity(sessionId, otherChildId, storyId, currentSceneId = "sc-3-$storyId"),
                )

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .`when`()
                    .post("/speaking-sessions/$sessionId/back")
                    .then()
                    .statusCode(404)
                    .body("detailCode", equalTo(ExceptionResponseCode.NOT_FOUND.detailCode))
            }

            test("토큰이 없으면 back은 401과 EMPTY_TOKEN을 반환한다") {
                RestAssured.given()
                    .contentType(ContentType.JSON)
                    .`when`()
                    .post("/speaking-sessions/any-session/back")
                    .then()
                    .statusCode(401)
                    .body("detailCode", equalTo(ExceptionResponseCode.EMPTY_TOKEN.detailCode))
                    .body("message", equalTo("토큰이 없습니다."))
            }
        }
    }
})
