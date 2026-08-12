package com.krince.reminisce.infra.adapter.`in`.controller

import com.krince.reminisce.domain.model.speakingsession.vo.SessionStatus
import com.krince.reminisce.infra.adapter.out.persistence.child.entity.ChildOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.childconsent.entity.ChildConsentOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.postactivityresult.entity.PostActivityResultOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.speakingsession.entity.SpeakingSessionOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.user.entity.UserOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.wordbook.entity.SavedWordOrmEntity
import com.krince.reminisce.infra.config.properties.FileStorageProperties
import com.krince.reminisce.shared.response.ExceptionResponseCode
import com.krince.reminisce.shared.util.UuidGenerator
import com.krince.reminisce.testutil.TestConfig
import com.krince.reminisce.testutil.fixture.TestChildConsentFixture
import com.krince.reminisce.testutil.fixture.TestChildFixture
import com.krince.reminisce.testutil.fixture.TestJwtTokenFixture
import com.krince.reminisce.testutil.fixture.TestPostActivityResultFixture
import com.krince.reminisce.testutil.fixture.TestSavedWordFixture
import com.krince.reminisce.testutil.fixture.TestSpeakingSessionFixture
import com.krince.reminisce.testutil.fixture.TestUserFixture
import io.kotest.core.annotation.DisplayName
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.parsing.Parser
import org.hamcrest.Matchers.equalTo
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("localtest")
@Import(TestConfig::class)
@io.kotest.core.annotation.Tags("test", "integrationTest")
@DisplayName("ConsentControllerImpl 통합테스트")
class ConsentControllerImplTest(
    @param:LocalServerPort private val port: Int,
    private val testUserFixture: TestUserFixture,
    private val testJwtTokenFixture: TestJwtTokenFixture,
    private val testChildFixture: TestChildFixture,
    private val testChildConsentFixture: TestChildConsentFixture,
    private val testSpeakingSessionFixture: TestSpeakingSessionFixture,
    private val testPostActivityResultFixture: TestPostActivityResultFixture,
    private val testSavedWordFixture: TestSavedWordFixture,
    private val fileStorageProperties: FileStorageProperties,
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

    fun childEntity(childId: String, guardianId: String): ChildOrmEntity = ChildOrmEntity(
        childId = childId,
        guardianId = guardianId,
        nickname = "테스트아이",
        birthYear = 2018,
    )

    fun consentEntity(childId: String): ChildConsentOrmEntity = ChildConsentOrmEntity(
        consentId = UuidGenerator.generate(),
        childId = childId,
        consentVersion = "v1.0",
        verificationMethod = "AUTHENTICATED_PARENT",
        consentedAt = LocalDateTime.of(2026, 6, 1, 0, 0),
    )

    fun sessionEntity(childId: String): SpeakingSessionOrmEntity = SpeakingSessionOrmEntity(
        sessionId = UuidGenerator.generate(),
        childId = childId,
        storyId = "story-${UuidGenerator.generate()}",
        status = SessionStatus.COMPLETED.name,
        startedAt = LocalDateTime.of(2026, 6, 1, 0, 0),
        lastActivityAt = LocalDateTime.of(2026, 6, 1, 0, 30),
    )

    fun postActivityResultEntity(sessionId: String, audioUrl: String): PostActivityResultOrmEntity =
        PostActivityResultOrmEntity(
            id = UuidGenerator.generate(),
            sessionId = sessionId,
            submittedOrder = listOf("card-1", "card-2"),
            isOrderCorrect = true,
            attemptCount = 1,
            retellingAudioUrl = audioUrl,
        )

    fun savedWordEntity(childId: String): SavedWordOrmEntity = SavedWordOrmEntity(
        savedWordId = UuidGenerator.generate(),
        childId = childId,
        word = "며느리",
        meaning = "아들의 아내",
    )

    fun createAudioFile(): String {
        val fileName = "retelling-${uniqueSuffix()}.m4a"
        val targetPath: Path = Paths.get(fileStorageProperties.path, fileName)
        Files.createDirectories(targetPath.parent)
        Files.write(targetPath, byteArrayOf(1, 2, 3))

        return "/files/$fileName"
    }

    fun audioFileExists(audioUrl: String): Boolean {
        val fileName: String = audioUrl.removePrefix("/files/")

        return Files.exists(Paths.get(fileStorageProperties.path, fileName))
    }

    fun authorizedTokenWithGuardian(): Pair<String, String> {
        val guardianId = "guardian-${uniqueSuffix()}"
        testUserFixture.saveUser(userEntity(guardianId))

        return Pair(testJwtTokenFixture.generateAccessToken(guardianId), guardianId)
    }

    beforeSpec {
        RestAssured.port = port
        RestAssured.basePath = "/api"
        RestAssured.defaultParser = Parser.JSON
    }

    beforeTest {
        testPostActivityResultFixture.deleteAllBatch()
        testSpeakingSessionFixture.deleteAllBatch()
        testSavedWordFixture.deleteAllBatch()
        testChildConsentFixture.deleteAllBatch()
        testChildFixture.deleteAllBatch()
        testUserFixture.deleteAllBatch()
    }

    context("withdrawConsent") {
        context("성공") {
            test("동의·세션·후속활동(음성)·단어가 있는 자기 아이를 철회하면 204이고 학습데이터·음성이 파기되며 아이·동의는 남는다") {
                val (token, guardianId) = authorizedTokenWithGuardian()
                val childId = "child-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))
                testChildConsentFixture.saveConsent(consentEntity(childId))
                val session = testSpeakingSessionFixture.save(sessionEntity(childId))
                val audioUrl = createAudioFile()
                testPostActivityResultFixture.save(postActivityResultEntity(session.sessionId, audioUrl))
                testSavedWordFixture.save(savedWordEntity(childId))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .`when`()
                    .post("/children/$childId/consent/withdrawal")
                    .then()
                    .statusCode(204)

                testSpeakingSessionFixture.findBySessionId(session.sessionId) shouldBe null
                testPostActivityResultFixture.findBySessionId(session.sessionId) shouldBe null
                testSavedWordFixture.findAllByChildId(childId).size shouldBe 0
                audioFileExists(audioUrl) shouldBe false

                testChildConsentFixture.existsActiveByChildId(childId) shouldBe false
                testChildFixture.findAllByGuardianId(guardianId).size shouldBe 1
                val consents = testChildConsentFixture.findAllByChildId(childId)
                consents.size shouldBe 1
                (consents[0].withdrawnAt != null) shouldBe true
            }
        }

        context("예외케이스") {
            test("남의 아이 childId로 철회하면 404 NOT_FOUND_CHILD이고 그 아이 동의는 활성으로 남는다") {
                val (token, _) = authorizedTokenWithGuardian()
                val otherGuardianId = "other-guardian-${uniqueSuffix()}"
                testUserFixture.saveUser(userEntity(otherGuardianId))
                val otherChildId = "child-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(otherChildId, otherGuardianId))
                testChildConsentFixture.saveConsent(consentEntity(otherChildId))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .`when`()
                    .post("/children/$otherChildId/consent/withdrawal")
                    .then()
                    .statusCode(404)
                    .body("success", equalTo(false))
                    .body("code", equalTo(404))
                    .body("detailCode", equalTo(ExceptionResponseCode.NOT_FOUND_CHILD.detailCode))

                testChildConsentFixture.existsActiveByChildId(otherChildId) shouldBe true
            }

            test("활성 동의가 없는 자기 아이를 철회하면 422 BUSINESS_RULE_VIOLATION이다") {
                val (token, guardianId) = authorizedTokenWithGuardian()
                val childId = "child-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .`when`()
                    .post("/children/$childId/consent/withdrawal")
                    .then()
                    .statusCode(422)
                    .body("success", equalTo(false))
                    .body("code", equalTo(422))
                    .body("detailCode", equalTo(ExceptionResponseCode.BUSINESS_RULE_VIOLATION.detailCode))
            }

            test("토큰이 없으면 401 EMPTY_TOKEN이다") {
                RestAssured.given()
                    .contentType(ContentType.JSON)
                    .`when`()
                    .post("/children/any-child-id/consent/withdrawal")
                    .then()
                    .statusCode(401)
                    .body("success", equalTo(false))
                    .body("code", equalTo(401))
                    .body("detailCode", equalTo(ExceptionResponseCode.EMPTY_TOKEN.detailCode))
            }
        }
    }
})
