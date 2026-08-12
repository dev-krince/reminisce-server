package com.krince.reminisce.infra.adapter.`in`.controller

import com.krince.reminisce.infra.adapter.out.persistence.child.entity.ChildOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.user.entity.UserOrmEntity
import com.krince.reminisce.shared.response.ExceptionResponseCode
import com.krince.reminisce.shared.response.SuccessResponseCode
import com.krince.reminisce.testutil.TestConfig
import com.krince.reminisce.testutil.fixture.TestChildFixture
import com.krince.reminisce.testutil.fixture.TestJwtTokenFixture
import com.krince.reminisce.testutil.fixture.TestSavedWordFixture
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

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("localtest")
@Import(TestConfig::class)
@io.kotest.core.annotation.Tags("test", "integrationTest")
@DisplayName("WordbookControllerImpl 통합테스트")
class WordbookControllerImplTest(
    @param:LocalServerPort private val port: Int,
    private val testUserFixture: TestUserFixture,
    private val testJwtTokenFixture: TestJwtTokenFixture,
    private val testChildFixture: TestChildFixture,
    private val testSavedWordFixture: TestSavedWordFixture,
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

    fun authorizedTokenWithGuardian(): Pair<String, String> {
        val guardianId = "guardian-${uniqueSuffix()}"
        testUserFixture.saveUser(userEntity(guardianId))

        return Pair(testJwtTokenFixture.generateAccessToken(guardianId), guardianId)
    }

    fun saveWordRequest(word: String, meaning: String?, sourceSceneId: String?): Map<String, String?> =
        mapOf("word" to word, "meaning" to meaning, "sourceSceneId" to sourceSceneId)

    beforeSpec {
        RestAssured.port = port
        RestAssured.basePath = "/api"
        RestAssured.defaultParser = Parser.JSON
    }

    beforeTest {
        testSavedWordFixture.deleteAllBatch()
        testChildFixture.deleteAllBatch()
        testUserFixture.deleteAllBatch()
    }

    context("saveWord") {
        context("성공") {
            test("자기 아이에 단어를 저장하면 201과 저장 필드를 반환하고 DB에 저장된다") {
                val (token, guardianId) = authorizedTokenWithGuardian()
                val childId = "child-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .body(saveWordRequest("며느리", "아들의 아내", "sc-1"))
                    .`when`()
                    .post("/children/$childId/words")
                    .then()
                    .statusCode(201)
                    .body("success", equalTo(true))
                    .body("code", equalTo(201))
                    .body("message", equalTo(SuccessResponseCode.CREATED.message))
                    .body("data.savedWordId", notNullValue())
                    .body("data.word", equalTo("며느리"))
                    .body("data.meaning", equalTo("아들의 아내"))
                    .body("data.sourceSceneId", equalTo("sc-1"))
                    .body("data.createdAt", notNullValue())

                val stored = testSavedWordFixture.findAllByChildId(childId)
                stored shouldHaveSize 1
                stored[0].word shouldBe "며느리"
                stored[0].meaning shouldBe "아들의 아내"
                stored[0].sourceSceneId shouldBe "sc-1"
            }

            test("meaning·sourceSceneId 없이 단어만 저장하면 201과 null 필드로 응답한다") {
                val (token, guardianId) = authorizedTokenWithGuardian()
                val childId = "child-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .body(saveWordRequest("배나무", null, null))
                    .`when`()
                    .post("/children/$childId/words")
                    .then()
                    .statusCode(201)
                    .body("data.word", equalTo("배나무"))
                    .body("data.meaning", equalTo(null))
                    .body("data.sourceSceneId", equalTo(null))

                val stored = testSavedWordFixture.findAllByChildId(childId)
                stored shouldHaveSize 1
                stored[0].meaning shouldBe null
                stored[0].sourceSceneId shouldBe null
            }
        }

        context("예외케이스") {
            test("남의 아이 childId로 저장하면 404와 NOT_FOUND를 반환하고 저장되지 않는다") {
                val (token, _) = authorizedTokenWithGuardian()
                val otherGuardianId = "other-guardian-${uniqueSuffix()}"
                testUserFixture.saveUser(userEntity(otherGuardianId))
                val otherChildId = "child-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(otherChildId, otherGuardianId))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .body(saveWordRequest("며느리", "아들의 아내", null))
                    .`when`()
                    .post("/children/$otherChildId/words")
                    .then()
                    .statusCode(404)
                    .body("success", equalTo(false))
                    .body("code", equalTo(404))
                    .body("detailCode", equalTo(ExceptionResponseCode.NOT_FOUND.detailCode))

                testSavedWordFixture.findAllByChildId(otherChildId) shouldHaveSize 0
            }

            test("토큰이 없으면 401과 EMPTY_TOKEN을 반환한다") {
                RestAssured.given()
                    .contentType(ContentType.JSON)
                    .body(saveWordRequest("며느리", null, null))
                    .`when`()
                    .post("/children/any-child-id/words")
                    .then()
                    .statusCode(401)
                    .body("success", equalTo(false))
                    .body("code", equalTo(401))
                    .body("detailCode", equalTo(ExceptionResponseCode.EMPTY_TOKEN.detailCode))
                    .body("message", equalTo("토큰이 없습니다."))
            }
        }
    }

    context("getWordbook") {
        context("성공") {
            test("자기 아이의 단어장을 최근순으로 반환한다") {
                val (token, guardianId) = authorizedTokenWithGuardian()
                val childId = "child-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .body(saveWordRequest("며느리", "아들의 아내", "sc-1"))
                    .`when`()
                    .post("/children/$childId/words")
                    .then()
                    .statusCode(201)

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .body(saveWordRequest("배나무", "배가 열리는 나무", "sc-2"))
                    .`when`()
                    .post("/children/$childId/words")
                    .then()
                    .statusCode(201)

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .`when`()
                    .get("/children/$childId/words")
                    .then()
                    .statusCode(200)
                    .body("success", equalTo(true))
                    .body("code", equalTo(200))
                    .body("message", equalTo(SuccessResponseCode.OK.message))
                    .body("data", hasSize<Any>(2))
                    .body("data.word", contains("배나무", "며느리"))
                    .body("data[0].meaning", equalTo("배가 열리는 나무"))
                    .body("data[0].sourceSceneId", equalTo("sc-2"))

                testSavedWordFixture.findAllByChildId(childId) shouldHaveSize 2
            }

            test("저장한 단어가 없으면 빈 목록을 200으로 반환한다") {
                val (token, guardianId) = authorizedTokenWithGuardian()
                val childId = "child-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .`when`()
                    .get("/children/$childId/words")
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
                    .get("/children/$otherChildId/words")
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
                    .get("/children/any-child-id/words")
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
