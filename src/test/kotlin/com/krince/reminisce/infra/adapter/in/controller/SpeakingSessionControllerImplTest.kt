package com.krince.reminisce.infra.adapter.`in`.controller

import com.krince.reminisce.domain.model.story.vo.StoryStatus
import com.krince.reminisce.infra.adapter.out.persistence.child.entity.ChildOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.childconsent.entity.ChildConsentOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.story.entity.StoryOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.user.entity.UserOrmEntity
import com.krince.reminisce.shared.response.ExceptionResponseCode
import com.krince.reminisce.shared.response.SuccessResponseCode
import com.krince.reminisce.testutil.TestConfig
import com.krince.reminisce.testutil.fixture.TestChildConsentFixture
import com.krince.reminisce.testutil.fixture.TestChildFixture
import com.krince.reminisce.testutil.fixture.TestJwtTokenFixture
import com.krince.reminisce.testutil.fixture.TestSpeakingSessionFixture
import com.krince.reminisce.testutil.fixture.TestStoryFixture
import com.krince.reminisce.testutil.fixture.TestUserFixture
import io.kotest.core.annotation.DisplayName
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.parsing.Parser
import org.hamcrest.Matchers.equalTo
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

    beforeSpec {
        RestAssured.port = port
        RestAssured.basePath = "/api"
        RestAssured.defaultParser = Parser.JSON
    }

    beforeTest {
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
})
