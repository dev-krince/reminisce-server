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
import com.krince.reminisce.testutil.fixture.TestUserFixture
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.parsing.Parser
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@Tags("test", "integrationTest")
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("localtest")
@DisplayName("ProfileInterviewControllerImpl 통합테스트")
class ProfileInterviewControllerImplTest(
    @param:LocalServerPort private val port: Int,
    private val testUserFixture: TestUserFixture,
    private val testChildFixture: TestChildFixture,
    private val testChildConsentFixture: TestChildConsentFixture,
    private val testProfileInterviewFixture: TestProfileInterviewFixture,
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

    fun childEntity(childId: String, guardianId: String, nickname: String = "토토"): ChildOrmEntity =
        ChildOrmEntity(childId = childId, guardianId = guardianId, nickname = nickname, birthYear = 2019)

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

    beforeSpec {
        RestAssured.port = port
        RestAssured.basePath = "/api"
        RestAssured.defaultParser = Parser.JSON
    }

    beforeTest {
        testProfileInterviewFixture.deleteAllBatch()
        testChildConsentFixture.deleteAllBatch()
        testChildFixture.deleteAllBatch()
        testUserFixture.deleteAllBatch()
    }

    context("startProfileInterview") {
        context("성공") {
            test("동의 있는 내 아이로 시작하면 201과 이름이 들어간 첫 큐미 질문·음성을 반환하고 인터뷰·메시지가 저장된다") {
                val guardianId = "guardian-${uniqueSuffix()}"
                testUserFixture.saveUser(userEntity(guardianId))
                val token = testJwtTokenFixture.generateAccessToken(guardianId)
                val childId = "child-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))
                testChildConsentFixture.saveConsent(consentEntity(childId))

                val interviewId = RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .body(mapOf("childId" to childId))
                    .`when`()
                    .post("/profile-interviews")
                    .then()
                    .statusCode(201)
                    .body("success", equalTo(true))
                    .body("data.childId", equalTo(childId))
                    .body("data.status", equalTo("IN_PROGRESS"))
                    .body("data.stage", equalTo("FREE_TALK"))
                    .body(
                        "data.qumiText",
                        equalTo("안녕 토토야! 나는 큐미야! 오늘은 토토랑 재미있는 이야기를 만들어 볼 거야. 토토는 어떤 이야기를 좋아해?"),
                    )
                    .body("data.qumiAudio", notNullValue())
                    .extract()
                    .path<String>("data.interviewId")

                testProfileInterviewFixture.findAllInterviewsByChildId(childId).size shouldBe 1
                val messages = testProfileInterviewFixture.findMessagesByInterviewId(interviewId)
                messages.size shouldBe 1
                messages.first().speaker shouldBe "QUMI"
                messages.first().turnOrder shouldBe 1L
            }

            test("진행 중 인터뷰가 있으면 200과 같은 인터뷰를 반환하고 새로 만들지 않는다") {
                val guardianId = "guardian-${uniqueSuffix()}"
                testUserFixture.saveUser(userEntity(guardianId))
                val token = testJwtTokenFixture.generateAccessToken(guardianId)
                val childId = "child-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))
                testChildConsentFixture.saveConsent(consentEntity(childId))

                val firstInterviewId = RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .body(mapOf("childId" to childId))
                    .`when`()
                    .post("/profile-interviews")
                    .then()
                    .statusCode(201)
                    .extract()
                    .path<String>("data.interviewId")

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .body(mapOf("childId" to childId))
                    .`when`()
                    .post("/profile-interviews")
                    .then()
                    .statusCode(200)
                    .body("data.interviewId", equalTo(firstInterviewId))

                testProfileInterviewFixture.findAllInterviewsByChildId(childId).size shouldBe 1
            }
        }

        context("예외케이스") {
            test("동의가 없으면 422와 CONSENT_REQUIRED를 반환하고 인터뷰를 만들지 않는다") {
                val guardianId = "guardian-${uniqueSuffix()}"
                testUserFixture.saveUser(userEntity(guardianId))
                val token = testJwtTokenFixture.generateAccessToken(guardianId)
                val childId = "child-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .body(mapOf("childId" to childId))
                    .`when`()
                    .post("/profile-interviews")
                    .then()
                    .statusCode(422)
                    .body("detailCode", equalTo(ExceptionResponseCode.CONSENT_REQUIRED.detailCode))

                testProfileInterviewFixture.findAllInterviewsByChildId(childId).size shouldBe 0
            }

            test("타 보호자의 아이면 404와 NOT_FOUND_CHILD로 은닉한다") {
                val guardianId = "guardian-${uniqueSuffix()}"
                val otherGuardianId = "other-${uniqueSuffix()}"
                testUserFixture.saveUser(userEntity(guardianId))
                testUserFixture.saveUser(userEntity(otherGuardianId))
                val token = testJwtTokenFixture.generateAccessToken(guardianId)
                val otherChildId = "theirs-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(otherChildId, otherGuardianId))
                testChildConsentFixture.saveConsent(consentEntity(otherChildId))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .body(mapOf("childId" to otherChildId))
                    .`when`()
                    .post("/profile-interviews")
                    .then()
                    .statusCode(404)
                    .body("detailCode", equalTo(ExceptionResponseCode.NOT_FOUND_CHILD.detailCode))
            }

            test("토큰이 없으면 401과 EMPTY_TOKEN을 반환한다") {
                RestAssured.given()
                    .contentType(ContentType.JSON)
                    .body(mapOf("childId" to "any-child"))
                    .`when`()
                    .post("/profile-interviews")
                    .then()
                    .statusCode(401)
                    .body("detailCode", equalTo(ExceptionResponseCode.EMPTY_TOKEN.detailCode))
            }
        }
    }

    context("submitInterviewUtterance") {
        fun startInterview(token: String, childId: String): String =
            RestAssured.given()
                .header("Authorization", token)
                .contentType(ContentType.JSON)
                .body(mapOf("childId" to childId))
                .`when`()
                .post("/profile-interviews")
                .then()
                .statusCode(201)
                .extract()
                .path("data.interviewId")

        context("성공") {
            test("아이 발화를 제출하면 200과 큐미의 다음 말을 반환하고 아이·큐미 메시지가 쌓인다") {
                val guardianId = "guardian-${uniqueSuffix()}"
                testUserFixture.saveUser(userEntity(guardianId))
                val token = testJwtTokenFixture.generateAccessToken(guardianId)
                val childId = "child-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))
                testChildConsentFixture.saveConsent(consentEntity(childId))
                val interviewId = startInterview(token, childId)

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .body(mapOf("text" to "토끼요.", "sttRawText" to "토끼요"))
                    .`when`()
                    .post("/profile-interviews/$interviewId/utterances")
                    .then()
                    .statusCode(200)
                    .body("data.status", equalTo("IN_PROGRESS"))
                    .body("data.stage", equalTo("FREE_TALK"))
                    .body("data.qumiText", notNullValue())
                    .body("data.qumiAudio", notNullValue())

                val messages = testProfileInterviewFixture.findMessagesByInterviewId(interviewId)
                messages.size shouldBe 3
                messages.map { it.speaker } shouldBe listOf("QUMI", "CHILD", "QUMI")
            }

            test("단계별 목표 턴을 모두 답하면 인터뷰가 COMPLETED로 끝나며, 끝난 뒤 제출은 422다") {
                val guardianId = "guardian-${uniqueSuffix()}"
                testUserFixture.saveUser(userEntity(guardianId))
                val token = testJwtTokenFixture.generateAccessToken(guardianId)
                val childId = "child-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))
                testChildConsentFixture.saveConsent(consentEntity(childId))
                val interviewId = startInterview(token, childId)

                val totalTurns = InterviewStage.entries.sumOf { it.targetChildTurns }
                var lastStatus = ""
                var lastStage = ""
                repeat(totalTurns) { turn ->
                    val response = RestAssured.given()
                        .header("Authorization", token)
                        .contentType(ContentType.JSON)
                        .body(mapOf("text" to "대답 ${turn + 1}이에요."))
                        .`when`()
                        .post("/profile-interviews/$interviewId/utterances")
                        .then()
                        .statusCode(200)
                        .extract()
                    lastStatus = response.path("data.status")
                    lastStage = response.path("data.stage")
                }

                lastStatus shouldBe "COMPLETED"
                lastStage shouldBe "CLOSING"
                testProfileInterviewFixture.findMessagesByInterviewId(interviewId).size shouldBe 1 + totalTurns * 2

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .body(mapOf("text" to "한 마디 더요."))
                    .`when`()
                    .post("/profile-interviews/$interviewId/utterances")
                    .then()
                    .statusCode(422)
                    .body("detailCode", equalTo(ExceptionResponseCode.BUSINESS_RULE_VIOLATION.detailCode))
            }
        }

        context("예외케이스") {
            test("다른 보호자의 인터뷰에 제출하면 404로 은닉한다") {
                val ownerGuardianId = "guardian-${uniqueSuffix()}"
                val intruderGuardianId = "intruder-${uniqueSuffix()}"
                testUserFixture.saveUser(userEntity(ownerGuardianId))
                testUserFixture.saveUser(userEntity(intruderGuardianId))
                val ownerToken = testJwtTokenFixture.generateAccessToken(ownerGuardianId)
                val intruderToken = testJwtTokenFixture.generateAccessToken(intruderGuardianId)
                val childId = "child-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, ownerGuardianId))
                testChildConsentFixture.saveConsent(consentEntity(childId))
                val interviewId = startInterview(ownerToken, childId)

                RestAssured.given()
                    .header("Authorization", intruderToken)
                    .contentType(ContentType.JSON)
                    .body(mapOf("text" to "몰래 한 마디."))
                    .`when`()
                    .post("/profile-interviews/$interviewId/utterances")
                    .then()
                    .statusCode(404)
                    .body("detailCode", equalTo(ExceptionResponseCode.NOT_FOUND.detailCode))
            }

            test("없는 인터뷰면 404를 반환한다") {
                val guardianId = "guardian-${uniqueSuffix()}"
                testUserFixture.saveUser(userEntity(guardianId))
                val token = testJwtTokenFixture.generateAccessToken(guardianId)

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .body(mapOf("text" to "아무 말."))
                    .`when`()
                    .post("/profile-interviews/unknown-${uniqueSuffix()}/utterances")
                    .then()
                    .statusCode(404)
                    .body("detailCode", equalTo(ExceptionResponseCode.NOT_FOUND.detailCode))
            }
        }
    }
})
