package com.krince.reminisce.infra.adapter.`in`.controller

import com.krince.reminisce.infra.adapter.out.persistence.appconfig.AppConfigRepository
import com.krince.reminisce.infra.adapter.out.persistence.story.SceneRepository
import com.krince.reminisce.infra.adapter.out.persistence.story.entity.SceneOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.story.entity.StoryOrmEntity
import com.krince.reminisce.shared.response.ExceptionResponseCode
import com.krince.reminisce.testutil.fixture.TestStoryFixture
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.parsing.Parser
import org.hamcrest.Matchers.equalTo
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles

@Tags("test", "integrationTest")
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("localtest")
@DisplayName("AdminSettingsControllerImpl 통합테스트")
class AdminSettingsControllerImplTest(
    @param:LocalServerPort private val port: Int,
    private val appConfigRepository: AppConfigRepository,
    private val sceneRepository: SceneRepository,
    private val testStoryFixture: TestStoryFixture,
) : FunSpec({

    fun uniqueSuffix(): String = "${System.currentTimeMillis()}-${System.nanoTime()}"

    fun storyEntity(storyId: String): StoryOrmEntity = StoryOrmEntity(
        storyId = storyId,
        title = "제목-$storyId",
        summary = "요약",
        intro = "도입",
        situation = null,
        childRole = null,
        difficulty = "보통",
        estimatedMinutes = 20,
        representativeImageUrl = null,
        status = "PUBLISHED",
        storyGenre = null,
        postActivityConfig = null,
    )

    fun dialogueScene(storyId: String, sceneId: String): SceneOrmEntity = SceneOrmEntity(
        sceneId = sceneId,
        storyId = storyId,
        sceneOrder = 1,
        chapter = 1,
        sceneType = "DIALOGUE",
        sceneDescription = "대화 설명",
        title = null,
        characterName = "ch_test",
        characterDisplayName = "테스트 캐릭터",
        characterOpening = null,
        characterClosing = null,
        conflict = null,
        sceneGoal = "목표",
        requiredElements = null,
        preferredTurns = 2,
        maxTurns = 4,
        mission = null,
        characterVoice = null,
        imageUrl = null,
        characterImageUrl = "/files/char-test.png",
    )

    beforeSpec {
        RestAssured.port = port
        RestAssured.basePath = "/api"
        RestAssured.defaultParser = Parser.JSON
    }

    beforeTest {
        appConfigRepository.deleteAllInBatch()
    }

    context("interview-stage-turns") {
        test("토큰 없이 조회하면 기본 설정(자유대화1·경험1·아이질문1, 합계 3)을 반환한다") {
            RestAssured.given()
                .contentType(ContentType.JSON)
                .`when`()
                .get("/admin/interview-stage-turns")
                .then()
                .statusCode(200)
                .body("data.freeTalk", equalTo(1))
                .body("data.experience", equalTo(1))
                .body("data.storyListening", equalTo(0))
                .body("data.characterFeeling", equalTo(0))
                .body("data.storyContinuation", equalTo(0))
                .body("data.childQuestion", equalTo(1))
                .body("data.totalChildTurns", equalTo(3))
        }

        test("관리키가 맞으면 토큰 없이 변경되고 저장·재조회에 반영된다") {
            RestAssured.given()
                .contentType(ContentType.JSON)
                .body(
                    mapOf(
                        "adminKey" to "reminisce",
                        "freeTalk" to 2, "experience" to 1, "storyListening" to 0,
                        "characterFeeling" to 1, "storyContinuation" to 0, "childQuestion" to 1,
                    ),
                )
                .`when`()
                .put("/admin/interview-stage-turns")
                .then()
                .statusCode(200)
                .body("data.freeTalk", equalTo(2))
                .body("data.totalChildTurns", equalTo(5))

            RestAssured.given()
                .contentType(ContentType.JSON)
                .`when`()
                .get("/admin/interview-stage-turns")
                .then()
                .statusCode(200)
                .body("data.freeTalk", equalTo(2))
                .body("data.characterFeeling", equalTo(1))
                .body("data.totalChildTurns", equalTo(5))
        }

        test("관리키가 틀리면 403을 반환하고 설정이 바뀌지 않는다") {
            RestAssured.given()
                .contentType(ContentType.JSON)
                .body(
                    mapOf(
                        "adminKey" to "wrong-key",
                        "freeTalk" to 9, "experience" to 0, "storyListening" to 0,
                        "characterFeeling" to 0, "storyContinuation" to 0, "childQuestion" to 0,
                    ),
                )
                .`when`()
                .put("/admin/interview-stage-turns")
                .then()
                .statusCode(403)
                .body("detailCode", equalTo(ExceptionResponseCode.FORBIDDEN.detailCode))

            appConfigRepository.count() shouldBe 0L
        }

        test("전부 0이면 400을 반환한다") {
            RestAssured.given()
                .contentType(ContentType.JSON)
                .body(
                    mapOf(
                        "adminKey" to "reminisce",
                        "freeTalk" to 0, "experience" to 0, "storyListening" to 0,
                        "characterFeeling" to 0, "storyContinuation" to 0, "childQuestion" to 0,
                    ),
                )
                .`when`()
                .put("/admin/interview-stage-turns")
                .then()
                .statusCode(400)
        }

        test("범위(0~10)를 벗어나면 400을 반환한다") {
            RestAssured.given()
                .contentType(ContentType.JSON)
                .body(
                    mapOf(
                        "adminKey" to "reminisce",
                        "freeTalk" to 11, "experience" to 1, "storyListening" to 0,
                        "characterFeeling" to 0, "storyContinuation" to 0, "childQuestion" to 1,
                    ),
                )
                .`when`()
                .put("/admin/interview-stage-turns")
                .then()
                .statusCode(400)
        }
    }

    context("scenes/{sceneId}/turns") {
        test("관리키가 맞으면 보낸 값만 바뀌고 DB에 반영된다") {
            val storyId = "story-${uniqueSuffix()}"
            val sceneId = "scene-${uniqueSuffix()}"
            testStoryFixture.saveStory(storyEntity(storyId))
            testStoryFixture.saveScene(dialogueScene(storyId, sceneId))

            RestAssured.given()
                .contentType(ContentType.JSON)
                .body(mapOf("adminKey" to "reminisce", "maxTurns" to 6))
                .`when`()
                .patch("/admin/scenes/$sceneId/turns")
                .then()
                .statusCode(200)
                .body("data.sceneId", equalTo(sceneId))
                .body("data.preferredTurns", equalTo(2))
                .body("data.maxTurns", equalTo(6))

            val stored = sceneRepository.findById(sceneId).orElseThrow()
            stored.preferredTurns shouldBe 2.toShort()
            stored.maxTurns shouldBe 6.toShort()
        }

        test("최소가 최대보다 크면 400을 반환한다") {
            val storyId = "story-${uniqueSuffix()}"
            val sceneId = "scene-${uniqueSuffix()}"
            testStoryFixture.saveStory(storyEntity(storyId))
            testStoryFixture.saveScene(dialogueScene(storyId, sceneId))

            RestAssured.given()
                .contentType(ContentType.JSON)
                .body(mapOf("adminKey" to "reminisce", "preferredTurns" to 5))
                .`when`()
                .patch("/admin/scenes/$sceneId/turns")
                .then()
                .statusCode(400)
        }

        test("없는 장면이면 404를 반환한다") {
            RestAssured.given()
                .contentType(ContentType.JSON)
                .body(mapOf("adminKey" to "reminisce", "maxTurns" to 5))
                .`when`()
                .patch("/admin/scenes/none-${uniqueSuffix()}/turns")
                .then()
                .statusCode(404)
        }
    }
})
