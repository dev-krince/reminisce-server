package com.krince.reminisce.infra.adapter.`in`.controller

import com.krince.reminisce.domain.model.speakingsession.vo.SceneEndReason
import com.krince.reminisce.domain.model.speakingsession.vo.SessionStatus
import com.krince.reminisce.domain.model.story.vo.SceneType
import com.krince.reminisce.domain.model.story.vo.StoryStatus
import com.krince.reminisce.domain.model.story.vo.ThinkingElement
import com.krince.reminisce.infra.adapter.out.persistence.child.entity.ChildOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.childconsent.entity.ChildConsentOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.speakingsession.entity.SpeakingSessionOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.story.entity.SceneOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.story.entity.StoryOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.user.entity.UserOrmEntity
import com.krince.reminisce.shared.response.ExceptionResponseCode
import com.krince.reminisce.shared.response.SuccessResponseCode
import com.krince.reminisce.testutil.TestConfig
import com.krince.reminisce.testutil.fixture.TestChildConsentFixture
import com.krince.reminisce.testutil.fixture.TestChildFixture
import com.krince.reminisce.testutil.fixture.TestJwtTokenFixture
import com.krince.reminisce.testutil.fixture.TestMessageFixture
import com.krince.reminisce.testutil.fixture.TestSpeakingSessionFixture
import com.krince.reminisce.testutil.fixture.TestStoryFixture
import com.krince.reminisce.testutil.fixture.TestUserFixture
import com.krince.reminisce.testutil.fixture.TestUtteranceAnalysisFixture
import io.kotest.core.annotation.DisplayName
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.parsing.Parser
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
    private val testJwtTokenFixture: TestJwtTokenFixture,
) : FunSpec({

    val consentVersion = "v1.0"
    val authenticatedParent = "AUTHENTICATED_PARENT"
    val defaultBirthYear: Short = 2019

    fun uniqueSuffix(): String = "${System.currentTimeMillis()}-${System.nanoTime()}"

    fun userEntity(userId: String): UserOrmEntity =
        UserOrmEntity(
            userId = userId,
            email = "$userId@example.com",
            password = "\$2a\$10\$hashedvaluehashedvaluehashedvalue",
            nickname = "홍길동",
            provider = "LOCAL",
            role = "ROLE_USER",
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

    fun storyEntity(storyId: String, status: String = StoryStatus.PUBLISHED.name): StoryOrmEntity =
        StoryOrmEntity(
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
            postActivityConfig = null,
        )

    fun authorizedGuardian(): Pair<String, String> {
        val guardianId = "guardian-${uniqueSuffix()}"
        testUserFixture.saveUser(userEntity(guardianId))

        return guardianId to testJwtTokenFixture.generateAccessToken(guardianId)
    }

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
        requiredElements = listOf(ThinkingElement.PERSPECTIVE, ThinkingElement.EMOTION),
        preferredTurns = null,
        maxTurns = 4,
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
    ): SpeakingSessionOrmEntity = SpeakingSessionOrmEntity(
        sessionId = sessionId,
        childId = childId,
        storyId = storyId,
        currentSceneId = currentSceneId,
        status = SessionStatus.IN_PROGRESS.name,
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
        testUtteranceAnalysisFixture.deleteAllBatch()
        testMessageFixture.deleteAllBatch()
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

    context("submitUtterance") {
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
                    .contentType(ContentType.JSON)
                    .body(mapOf("audio" to "  며느리가 참 힘들었겠어요  "))
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

                testMessageFixture.countBySessionId(sessionId) shouldBe 2L
                val stored = testMessageFixture.findAllBySessionId(sessionId).first()
                stored.speakerType shouldBe "CHILD"
                stored.text shouldBe "며느리가 참 힘들었겠어요"
                stored.sttRawText shouldBe "며느리가 참 힘들었겠어요"

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
                    .contentType(ContentType.JSON)
                    .body(mapOf("audio" to "며느리가 참 힘들었겠어요"))
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
                    .contentType(ContentType.JSON)
                    .body(mapOf("audio" to "며느리 입장에서 생각하면 참 힘들었겠어요"))
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
                val emotionOnlyAudio = "며느리가 참 힘들었겠어요"

                repeat(3) {
                    RestAssured.given()
                        .header("Authorization", token)
                        .contentType(ContentType.JSON)
                        .body(mapOf("audio" to emotionOnlyAudio))
                        .`when`()
                        .post("/speaking-sessions/$sessionId/utterances")
                        .then()
                        .statusCode(201)
                        .body("data.sceneEndReason", nullValue())
                }

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .body(mapOf("audio" to emotionOnlyAudio))
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
                    .body("data.characterReply.text", equalTo("그래도 아직은 못 말하겠어. 조금만 더 참아 볼게."))

                val storedSession = testSpeakingSessionFixture.findBySessionId(sessionId)
                storedSession?.currentChildTurnCount shouldBe 4
                storedSession?.sceneEndReason shouldBe "MAX_TURNS"
                storedSession?.lastResponseMode shouldBe "CLOSING"
                storedSession?.status shouldBe SessionStatus.IN_PROGRESS.name
            }
        }

        context("예외케이스") {
            test("blank 발화(STT 실패)는 422와 STT_TRANSCRIPTION_FAILED를 반환하고 메시지가 생성되지 않는다") {
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
                    .body(mapOf("audio" to "   "))
                    .`when`()
                    .post("/speaking-sessions/$sessionId/utterances")
                    .then()
                    .statusCode(422)
                    .body("success", equalTo(false))
                    .body("code", equalTo(422))
                    .body("detailCode", equalTo(ExceptionResponseCode.STT_TRANSCRIPTION_FAILED.detailCode))
                    .body("message", equalTo("음성 인식에 실패해 발화를 저장할 수 없습니다."))

                testMessageFixture.countBySessionId(sessionId) shouldBe 0L
                testUtteranceAnalysisFixture.count() shouldBe 0L
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
                    .contentType(ContentType.JSON)
                    .body(mapOf("audio" to "며느리가 참 힘들었겠어요"))
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
                    .contentType(ContentType.JSON)
                    .body(mapOf("audio" to "며느리가 참 힘들었겠어요"))
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
                    .contentType(ContentType.JSON)
                    .body(mapOf("audio" to "며느리가 참 힘들었겠어요"))
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
                    .contentType(ContentType.JSON)
                    .body(mapOf("audio" to "며느리가 참 힘들었겠어요"))
                    .`when`()
                    .post("/speaking-sessions/missing-${uniqueSuffix()}/utterances")
                    .then()
                    .statusCode(404)
                    .body("detailCode", equalTo(ExceptionResponseCode.NOT_FOUND.detailCode))
            }

            test("토큰이 없으면 401과 EMPTY_TOKEN을 반환한다") {
                RestAssured.given()
                    .contentType(ContentType.JSON)
                    .body(mapOf("audio" to "any"))
                    .`when`()
                    .post("/speaking-sessions/any-session/utterances")
                    .then()
                    .statusCode(401)
                    .body("detailCode", equalTo(ExceptionResponseCode.EMPTY_TOKEN.detailCode))
                    .body("message", equalTo("토큰이 없습니다."))
            }
        }
    }
})
