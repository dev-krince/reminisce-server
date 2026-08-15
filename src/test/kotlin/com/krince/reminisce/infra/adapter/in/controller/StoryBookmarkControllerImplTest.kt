package com.krince.reminisce.infra.adapter.`in`.controller

import com.krince.reminisce.domain.model.story.vo.StoryStatus
import com.krince.reminisce.infra.adapter.out.persistence.child.entity.ChildOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.savedstory.entity.SavedStoryOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.story.entity.StoryOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.user.entity.UserOrmEntity
import com.krince.reminisce.shared.response.ExceptionResponseCode
import com.krince.reminisce.shared.response.SuccessResponseCode
import com.krince.reminisce.testutil.TestConfig
import com.krince.reminisce.testutil.fixture.TestChildFixture
import com.krince.reminisce.testutil.fixture.TestJwtTokenFixture
import com.krince.reminisce.testutil.fixture.TestSavedStoryFixture
import com.krince.reminisce.testutil.fixture.TestStoryFixture
import com.krince.reminisce.testutil.fixture.TestUserFixture
import io.kotest.core.annotation.DisplayName
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.parsing.Parser
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.notNullValue
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("localtest")
@Import(TestConfig::class)
@io.kotest.core.annotation.Tags("test", "integrationTest")
@DisplayName("StoryBookmarkControllerImpl 통합테스트")
class StoryBookmarkControllerImplTest(
    @param:LocalServerPort private val port: Int,
    private val testUserFixture: TestUserFixture,
    private val testJwtTokenFixture: TestJwtTokenFixture,
    private val testChildFixture: TestChildFixture,
    private val testSavedStoryFixture: TestSavedStoryFixture,
    private val testStoryFixture: TestStoryFixture,
) : FunSpec({

    val concurrentRequestCount = 8

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

    fun childEntity(childId: String, guardianId: String): ChildOrmEntity = ChildOrmEntity(
        childId = childId,
        guardianId = guardianId,
        nickname = "테스트아이",
        birthYear = 2018,
    )

    fun authorizedTokenWithGuardian(): Pair<String, String> {
        val guardianId = "guardian-${uniqueSuffix()}"
        testUserFixture.saveUser(userEntity(guardianId))

        return Pair(testJwtTokenFixture.generateAccessToken(guardianId), guardianId)
    }

    fun addBookmarkRequest(storyId: String): Map<String, String> = mapOf("storyId" to storyId)

    fun publishedStoryEntity(storyId: String): StoryOrmEntity = StoryOrmEntity(
        storyId = storyId,
        title = "제목-$storyId",
        summary = "요약-$storyId",
        intro = "도입-$storyId",
        situation = null,
        childRole = null,
        difficulty = "보통",
        estimatedMinutes = 20,
        representativeImageUrl = "/files/$storyId.png",
        status = StoryStatus.PUBLISHED.name,
        storyGenre = null,
        postActivityConfig = null,
    )

    fun draftStoryEntity(storyId: String): StoryOrmEntity = StoryOrmEntity(
        storyId = storyId,
        title = "제목-$storyId",
        summary = "요약-$storyId",
        intro = "도입-$storyId",
        situation = null,
        childRole = null,
        difficulty = "보통",
        estimatedMinutes = 20,
        representativeImageUrl = "/files/$storyId.png",
        status = StoryStatus.DRAFT.name,
        storyGenre = null,
        postActivityConfig = null,
    )

    beforeSpec {
        RestAssured.port = port
        RestAssured.basePath = "/api"
        RestAssured.defaultParser = Parser.JSON
    }

    beforeTest {
        testSavedStoryFixture.deleteAllBatch()
        testStoryFixture.deleteAllBatch()
        testChildFixture.deleteAllBatch()
        testUserFixture.deleteAllBatch()
    }

    context("addBookmark") {
        context("성공") {
            test("자기 아이가 이야기를 찜하면 201과 저장 필드를 반환하고 DB에 저장된다") {
                val (token, guardianId) = authorizedTokenWithGuardian()
                val childId = "child-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))
                testStoryFixture.saveStory(publishedStoryEntity("s_story_1"))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .body(addBookmarkRequest("s_story_1"))
                    .`when`()
                    .post("/children/$childId/bookmarked-stories")
                    .then()
                    .statusCode(201)
                    .body("success", equalTo(true))
                    .body("code", equalTo(201))
                    .body("message", equalTo(SuccessResponseCode.CREATED.message))
                    .body("data.savedStoryId", notNullValue())
                    .body("data.storyId", equalTo("s_story_1"))
                    .body("data.createdAt", notNullValue())

                val stored = testSavedStoryFixture.findAllByChildId(childId)
                stored shouldHaveSize 1
                stored[0].storyId shouldBe "s_story_1"
            }

            test("이미 찜한 이야기를 다시 찜해도 201로 응답하고 행이 하나만 남는다") {
                val (token, guardianId) = authorizedTokenWithGuardian()
                val childId = "child-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))
                testStoryFixture.saveStory(publishedStoryEntity("s_story_1"))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .body(addBookmarkRequest("s_story_1"))
                    .`when`()
                    .post("/children/$childId/bookmarked-stories")
                    .then()
                    .statusCode(201)

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .body(addBookmarkRequest("s_story_1"))
                    .`when`()
                    .post("/children/$childId/bookmarked-stories")
                    .then()
                    .statusCode(201)
                    .body("data.storyId", equalTo("s_story_1"))

                testSavedStoryFixture.findAllByChildId(childId) shouldHaveSize 1
            }

            test("같은 이야기를 동시에 찜해도 모두 성공하고 행이 하나만 남는다") {
                val (token, guardianId) = authorizedTokenWithGuardian()
                val childId = "child-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))
                testStoryFixture.saveStory(publishedStoryEntity("s_story_1"))

                val ready = CountDownLatch(concurrentRequestCount)
                val start = CountDownLatch(1)
                val executor = Executors.newFixedThreadPool(concurrentRequestCount)
                val statuses = ConcurrentLinkedQueue<Int>()

                val futures = (1..concurrentRequestCount).map {
                    executor.submit {
                        ready.countDown()
                        start.await()
                        val status = RestAssured.given()
                            .header("Authorization", token)
                            .contentType(ContentType.JSON)
                            .body(addBookmarkRequest("s_story_1"))
                            .`when`()
                            .post("/children/$childId/bookmarked-stories")
                            .then()
                            .extract()
                            .statusCode()
                        statuses.add(status)
                    }
                }

                ready.await()
                start.countDown()
                futures.forEach { it.get() }
                executor.shutdown()

                statuses.forEach { it shouldBe 201 }
                testSavedStoryFixture.findAllByChildId(childId) shouldHaveSize 1
            }
        }

        context("예외케이스") {
            test("남의 아이 childId로 찜하면 404와 NOT_FOUND를 반환하고 저장되지 않는다") {
                val (token, _) = authorizedTokenWithGuardian()
                val otherGuardianId = "other-guardian-${uniqueSuffix()}"
                testUserFixture.saveUser(userEntity(otherGuardianId))
                val otherChildId = "child-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(otherChildId, otherGuardianId))
                testStoryFixture.saveStory(publishedStoryEntity("s_story_1"))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .body(addBookmarkRequest("s_story_1"))
                    .`when`()
                    .post("/children/$otherChildId/bookmarked-stories")
                    .then()
                    .statusCode(404)
                    .body("success", equalTo(false))
                    .body("code", equalTo(404))
                    .body("detailCode", equalTo(ExceptionResponseCode.NOT_FOUND.detailCode))

                testSavedStoryFixture.findAllByChildId(otherChildId) shouldHaveSize 0
            }

            test("존재하지 않는 storyId로 자기 아이가 찜하면 404 NOT_FOUND_STORY를 반환하고 저장되지 않는다") {
                val (token, guardianId) = authorizedTokenWithGuardian()
                val childId = "child-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .body(addBookmarkRequest("s_missing_${uniqueSuffix()}"))
                    .`when`()
                    .post("/children/$childId/bookmarked-stories")
                    .then()
                    .statusCode(404)
                    .body("success", equalTo(false))
                    .body("code", equalTo(404))
                    .body("detailCode", equalTo(ExceptionResponseCode.NOT_FOUND_STORY.detailCode))
                    .body("message", equalTo("이야기가 존재하지 않습니다."))

                testSavedStoryFixture.findAllByChildId(childId) shouldHaveSize 0
            }

            test("DRAFT storyId로 자기 아이가 찜하면 존재하지 않는 id와 같은 404 NOT_FOUND_STORY로 은닉한다") {
                val (token, guardianId) = authorizedTokenWithGuardian()
                val childId = "child-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))
                val draftId = "s_draft_${uniqueSuffix()}"
                testStoryFixture.saveStory(draftStoryEntity(draftId))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .body(addBookmarkRequest(draftId))
                    .`when`()
                    .post("/children/$childId/bookmarked-stories")
                    .then()
                    .statusCode(404)
                    .body("success", equalTo(false))
                    .body("code", equalTo(404))
                    .body("detailCode", equalTo(ExceptionResponseCode.NOT_FOUND_STORY.detailCode))

                testSavedStoryFixture.findAllByChildId(childId) shouldHaveSize 0
            }

            test("토큰이 없으면 401과 EMPTY_TOKEN을 반환한다") {
                RestAssured.given()
                    .contentType(ContentType.JSON)
                    .body(addBookmarkRequest("s_story_1"))
                    .`when`()
                    .post("/children/any-child-id/bookmarked-stories")
                    .then()
                    .statusCode(401)
                    .body("success", equalTo(false))
                    .body("code", equalTo(401))
                    .body("detailCode", equalTo(ExceptionResponseCode.EMPTY_TOKEN.detailCode))
                    .body("message", equalTo("토큰이 없습니다."))
            }
        }
    }

    context("getBookmarks") {
        context("성공") {
            test("자기 아이의 찜 목록을 최근순으로 반환한다") {
                val (token, guardianId) = authorizedTokenWithGuardian()
                val childId = "child-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))
                testStoryFixture.saveStory(publishedStoryEntity("s_story_1"))
                testStoryFixture.saveStory(publishedStoryEntity("s_story_2"))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .body(addBookmarkRequest("s_story_1"))
                    .`when`()
                    .post("/children/$childId/bookmarked-stories")
                    .then()
                    .statusCode(201)

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .body(addBookmarkRequest("s_story_2"))
                    .`when`()
                    .post("/children/$childId/bookmarked-stories")
                    .then()
                    .statusCode(201)

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .`when`()
                    .get("/children/$childId/bookmarked-stories")
                    .then()
                    .statusCode(200)
                    .body("success", equalTo(true))
                    .body("code", equalTo(200))
                    .body("message", equalTo(SuccessResponseCode.OK.message))
                    .body("data", hasSize<Any>(2))
                    .body("data.storyId", contains("s_story_2", "s_story_1"))

                testSavedStoryFixture.findAllByChildId(childId) shouldHaveSize 2
            }

            test("찜한 이야기가 없으면 빈 목록을 200으로 반환한다") {
                val (token, guardianId) = authorizedTokenWithGuardian()
                val childId = "child-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .`when`()
                    .get("/children/$childId/bookmarked-stories")
                    .then()
                    .statusCode(200)
                    .body("success", equalTo(true))
                    .body("data", hasSize<Any>(0))
            }
        }

        context("예외케이스") {
            test("남의 아이 childId로 조회하면 404와 NOT_FOUND를 반환한다") {
                val (token, _) = authorizedTokenWithGuardian()
                val otherGuardianId = "other-guardian-${uniqueSuffix()}"
                testUserFixture.saveUser(userEntity(otherGuardianId))
                val otherChildId = "child-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(otherChildId, otherGuardianId))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .`when`()
                    .get("/children/$otherChildId/bookmarked-stories")
                    .then()
                    .statusCode(404)
                    .body("success", equalTo(false))
                    .body("code", equalTo(404))
                    .body("detailCode", equalTo(ExceptionResponseCode.NOT_FOUND.detailCode))
            }
        }
    }

    context("removeBookmark") {
        context("성공") {
            test("자기 아이의 찜을 해제하면 204를 반환하고 DB에서 삭제된다") {
                val (token, guardianId) = authorizedTokenWithGuardian()
                val childId = "child-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))
                testStoryFixture.saveStory(publishedStoryEntity("s_story_1"))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .body(addBookmarkRequest("s_story_1"))
                    .`when`()
                    .post("/children/$childId/bookmarked-stories")
                    .then()
                    .statusCode(201)

                RestAssured.given()
                    .header("Authorization", token)
                    .`when`()
                    .delete("/children/$childId/bookmarked-stories/s_story_1")
                    .then()
                    .statusCode(204)

                testSavedStoryFixture.findAllByChildId(childId) shouldHaveSize 0
            }

            test("찜하지 않은 이야기를 해제해도 204를 반환한다") {
                val (token, guardianId) = authorizedTokenWithGuardian()
                val childId = "child-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))

                RestAssured.given()
                    .header("Authorization", token)
                    .`when`()
                    .delete("/children/$childId/bookmarked-stories/s_unknown")
                    .then()
                    .statusCode(204)

                testSavedStoryFixture.findAllByChildId(childId) shouldHaveSize 0
            }
        }

        context("예외케이스") {
            test("남의 아이 childId로 해제하면 404와 NOT_FOUND를 반환한다") {
                val (token, _) = authorizedTokenWithGuardian()
                val otherGuardianId = "other-guardian-${uniqueSuffix()}"
                testUserFixture.saveUser(userEntity(otherGuardianId))
                val otherChildId = "child-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(otherChildId, otherGuardianId))
                testSavedStoryFixture.save(
                    SavedStoryOrmEntity(
                        savedStoryId = "saved-${uniqueSuffix()}",
                        childId = otherChildId,
                        storyId = "s_story_1",
                    ),
                )

                RestAssured.given()
                    .header("Authorization", token)
                    .`when`()
                    .delete("/children/$otherChildId/bookmarked-stories/s_story_1")
                    .then()
                    .statusCode(404)
                    .body("success", equalTo(false))
                    .body("code", equalTo(404))
                    .body("detailCode", equalTo(ExceptionResponseCode.NOT_FOUND.detailCode))

                testSavedStoryFixture.findAllByChildId(otherChildId) shouldHaveSize 1
            }

            test("토큰이 없으면 401과 EMPTY_TOKEN을 반환한다") {
                RestAssured.given()
                    .`when`()
                    .delete("/children/any-child-id/bookmarked-stories/s_story_1")
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
