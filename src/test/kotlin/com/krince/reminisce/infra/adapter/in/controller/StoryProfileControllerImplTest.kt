package com.krince.reminisce.infra.adapter.`in`.controller

import com.krince.reminisce.domain.model.profileinterview.vo.InterviewStage
import com.krince.reminisce.infra.adapter.out.persistence.child.entity.ChildOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.childconsent.entity.ChildConsentOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.user.entity.UserOrmEntity
import com.krince.reminisce.shared.response.ExceptionResponseCode
import com.krince.reminisce.testutil.fixture.TestChildConsentFixture
import com.krince.reminisce.testutil.fixture.TestChildFixture
import com.krince.reminisce.testutil.fixture.TestJwtTokenFixture
import com.krince.reminisce.testutil.fixture.TestProfileInterviewFixture
import com.krince.reminisce.testutil.fixture.TestStoryProfileFixture
import com.krince.reminisce.testutil.fixture.TestUserFixture
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.parsing.Parser
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@Tags("test", "integrationTest")
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("localtest")
@DisplayName("StoryProfileControllerImpl 통합테스트")
class StoryProfileControllerImplTest(
    @param:LocalServerPort private val port: Int,
    private val testUserFixture: TestUserFixture,
    private val testChildFixture: TestChildFixture,
    private val testChildConsentFixture: TestChildConsentFixture,
    private val testProfileInterviewFixture: TestProfileInterviewFixture,
    private val testStoryProfileFixture: TestStoryProfileFixture,
    private val testJwtTokenFixture: TestJwtTokenFixture,
) : FunSpec({

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
        ChildOrmEntity(childId = childId, guardianId = guardianId, nickname = "토토", birthYear = 2019)

    fun consentEntity(childId: String): ChildConsentOrmEntity =
        ChildConsentOrmEntity(
            consentId = "consent-$childId",
            childId = childId,
            consentVersion = "v1.0",
            verificationMethod = "AUTHENTICATED_PARENT",
            consentedAt = LocalDateTime.now().minusDays(1),
            withdrawnAt = null,
        )

    fun uniqueSuffix(): String = "${System.currentTimeMillis()}-${System.nanoTime()}"

    fun completeInterview(token: String, childId: String) {
        val interviewId: String = RestAssured.given()
            .header("Authorization", token)
            .contentType(ContentType.JSON)
            .body(mapOf("childId" to childId))
            .`when`()
            .post("/profile-interviews")
            .then()
            .statusCode(201)
            .extract()
            .path("data.interviewId")
        repeat(InterviewStage.entries.sumOf { it.targetChildTurns }) { turn ->
            RestAssured.given()
                .header("Authorization", token)
                .contentType(ContentType.JSON)
                .body(mapOf("text" to "대답 ${turn + 1}이에요."))
                .`when`()
                .post("/profile-interviews/$interviewId/utterances")
                .then()
                .statusCode(200)
        }
    }

    beforeSpec {
        RestAssured.port = port
        RestAssured.basePath = "/api"
        RestAssured.defaultParser = Parser.JSON
    }

    beforeTest {
        testStoryProfileFixture.deleteAllBatch()
        testProfileInterviewFixture.deleteAllBatch()
        testChildConsentFixture.deleteAllBatch()
        testChildFixture.deleteAllBatch()
        testUserFixture.deleteAllBatch()
    }

    context("getStoryProfile") {
        context("성공") {
            test("인터뷰를 완주한 뒤 조회하면 프로필을 분석·저장해 반환하고, 재조회해도 같은 프로필 1건이다") {
                val guardianId = "guardian-${uniqueSuffix()}"
                testUserFixture.saveUser(userEntity(guardianId))
                val token = testJwtTokenFixture.generateAccessToken(guardianId)
                val childId = "child-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))
                testChildConsentFixture.saveConsent(consentEntity(childId))
                completeInterview(token, childId)

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .`when`()
                    .get("/children/$childId/story-profile")
                    .then()
                    .statusCode(200)
                    .body("data.childId", equalTo(childId))
                    .body("data.interestTopics", hasSize<Any>(2))
                    .body("data.interestTopics[0].category", equalTo("관계"))
                    .body("data.strengths", hasSize<Any>(3))
                    .body("data.practicePoints", hasSize<Any>(3))
                    .body("data.speechAnalyses", hasSize<Any>(3))
                    .body("data.speechAnalyses[0].area", equalTo("어휘"))

                testStoryProfileFixture.count() shouldBe 1L

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .`when`()
                    .get("/children/$childId/story-profile")
                    .then()
                    .statusCode(200)
                    .body("data.childId", equalTo(childId))

                testStoryProfileFixture.count() shouldBe 1L
            }
        }

        context("예외케이스") {
            test("완료된 인터뷰가 없으면 404와 NOT_FOUND를 반환한다") {
                val guardianId = "guardian-${uniqueSuffix()}"
                testUserFixture.saveUser(userEntity(guardianId))
                val token = testJwtTokenFixture.generateAccessToken(guardianId)
                val childId = "child-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .`when`()
                    .get("/children/$childId/story-profile")
                    .then()
                    .statusCode(404)
                    .body("detailCode", equalTo(ExceptionResponseCode.NOT_FOUND.detailCode))
            }

            test("타 보호자의 아이면 404와 NOT_FOUND_CHILD로 은닉한다") {
                val guardianId = "guardian-${uniqueSuffix()}"
                val otherGuardianId = "other-${uniqueSuffix()}"
                testUserFixture.saveUser(userEntity(guardianId))
                testUserFixture.saveUser(userEntity(otherGuardianId))
                val token = testJwtTokenFixture.generateAccessToken(guardianId)
                val otherChildId = "theirs-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(otherChildId, otherGuardianId))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .`when`()
                    .get("/children/$otherChildId/story-profile")
                    .then()
                    .statusCode(404)
                    .body("detailCode", equalTo(ExceptionResponseCode.NOT_FOUND_CHILD.detailCode))
            }
        }
    }
})
