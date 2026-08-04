package com.krince.reminisce.infra.adapter.`in`.controller

import com.krince.reminisce.infra.adapter.out.persistence.child.entity.ChildOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.user.entity.UserOrmEntity
import com.krince.reminisce.infra.config.properties.ChildPolicyProperties
import com.krince.reminisce.shared.response.ExceptionResponseCode
import com.krince.reminisce.shared.response.SuccessResponseCode
import com.krince.reminisce.testutil.TestConfig
import com.krince.reminisce.testutil.fixture.TestChildFixture
import com.krince.reminisce.testutil.fixture.TestJwtTokenFixture
import com.krince.reminisce.testutil.fixture.TestUserFixture
import io.kotest.common.ExperimentalKotest
import io.kotest.core.annotation.DisplayName
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.parsing.Parser
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasKey
import org.hamcrest.Matchers.hasSize
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@OptIn(ExperimentalKotest::class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("localtest")
@Import(TestConfig::class)
@io.kotest.core.annotation.Tags("test", "integrationTest")
@DisplayName("ChildControllerImpl 통합테스트")
class ChildControllerImplTest(
    @param:LocalServerPort private val port: Int,
    private val testUserFixture: TestUserFixture,
    private val testChildFixture: TestChildFixture,
    private val testJwtTokenFixture: TestJwtTokenFixture,
    private val childPolicyProperties: ChildPolicyProperties,
) : FunSpec({

    val maxPerGuardian = childPolicyProperties.maxPerGuardian

    fun userEntity(userId: String): UserOrmEntity =
        UserOrmEntity(
            userId = userId,
            email = "$userId@example.com",
            password = "\$2a\$10\$hashedvaluehashedvaluehashedvalue",
            nickname = "홍길동",
            provider = "LOCAL",
            role = "ROLE_USER",
        )

    val defaultBirthYear: Short = 2019

    fun childEntity(
        childId: String,
        guardianId: String,
        nickname: String,
        birthYear: Short = defaultBirthYear,
    ): ChildOrmEntity =
        ChildOrmEntity(childId = childId, guardianId = guardianId, nickname = nickname, birthYear = birthYear)

    fun uniqueSuffix(): String = "${System.currentTimeMillis()}-${System.nanoTime()}"

    beforeSpec {
        RestAssured.port = port
        RestAssured.basePath = "/api"
        RestAssured.defaultParser = Parser.JSON
    }

    beforeTest {
        testChildFixture.deleteAllBatch()
        testUserFixture.deleteAllBatch()
    }

    context("registerChild") {
        context("성공") {
            test("유효한 토큰으로 아이를 등록하면 201과 아이 정보를 반환하고 DB에 본인 소유로 1건 저장된다") {
                val guardianId = "guardian-${uniqueSuffix()}"
                testUserFixture.saveUser(userEntity(guardianId))
                val token = testJwtTokenFixture.generateAccessToken(guardianId)

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .body(mapOf("nickname" to "토토", "birthYear" to 2019))
                    .`when`()
                    .post("/children")
                    .then()
                    .statusCode(201)
                    .body("success", equalTo(true))
                    .body("code", equalTo(201))
                    .body("message", equalTo(SuccessResponseCode.CREATED.message))
                    .body("data", hasKey("childId"))
                    .body("data", hasKey("nickname"))
                    .body("data", hasKey("birthYear"))
                    .body("data", hasKey("createdDate"))
                    .body("data.nickname", equalTo("토토"))
                    .body("data.birthYear", equalTo(2019))

                val stored = testChildFixture.findAllByGuardianId(guardianId)
                stored.size shouldBe 1
                stored.first().guardianId shouldBe guardianId
                stored.first().birthYear shouldBe 2019.toShort()
            }
        }
        context("예외케이스") {
            test("토큰이 없으면 401과 EMPTY_TOKEN을 반환한다") {
                RestAssured.given()
                    .contentType(ContentType.JSON)
                    .body(mapOf("nickname" to "토토", "birthYear" to 2019))
                    .`when`()
                    .post("/children")
                    .then()
                    .statusCode(401)
                    .body("success", equalTo(false))
                    .body("code", equalTo(401))
                    .body("detailCode", equalTo(ExceptionResponseCode.EMPTY_TOKEN.detailCode))
                    .body("message", equalTo("토큰이 없습니다."))
            }

            test("상한을 초과해 등록하면 422와 CHILD_LIMIT_EXCEEDED를 반환한다") {
                val guardianId = "guardian-${uniqueSuffix()}"
                testUserFixture.saveUser(userEntity(guardianId))
                val token = testJwtTokenFixture.generateAccessToken(guardianId)
                repeat(maxPerGuardian) { index ->
                    testChildFixture.saveChild(childEntity("child-$guardianId-$index", guardianId, "토토$index"))
                }

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .body(mapOf("nickname" to "코코", "birthYear" to 2019))
                    .`when`()
                    .post("/children")
                    .then()
                    .statusCode(422)
                    .body("success", equalTo(false))
                    .body("code", equalTo(422))
                    .body("detailCode", equalTo(ExceptionResponseCode.CHILD_LIMIT_EXCEEDED.detailCode))
                    .body("message", equalTo("등록 가능한 아이 수를 초과했습니다."))
            }

            test("미래연도로 등록하면 400과 INVALID_BIRTH_YEAR를 반환한다") {
                val guardianId = "guardian-${uniqueSuffix()}"
                testUserFixture.saveUser(userEntity(guardianId))
                val token = testJwtTokenFixture.generateAccessToken(guardianId)
                val futureYear = java.time.Year.now().value + 1

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .body(mapOf("nickname" to "토토", "birthYear" to futureYear))
                    .`when`()
                    .post("/children")
                    .then()
                    .statusCode(400)
                    .body("success", equalTo(false))
                    .body("code", equalTo(400))
                    .body("detailCode", equalTo(ExceptionResponseCode.INVALID_BIRTH_YEAR.detailCode))

                testChildFixture.findAllByGuardianId(guardianId).size shouldBe 0
            }

            test("구조를 위반한 출생연도로 등록하면 400과 INVALID_BIRTH_YEAR를 반환한다") {
                val guardianId = "guardian-${uniqueSuffix()}"
                testUserFixture.saveUser(userEntity(guardianId))
                val token = testJwtTokenFixture.generateAccessToken(guardianId)

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .body(mapOf("nickname" to "토토", "birthYear" to 1800))
                    .`when`()
                    .post("/children")
                    .then()
                    .statusCode(400)
                    .body("success", equalTo(false))
                    .body("code", equalTo(400))
                    .body("detailCode", equalTo(ExceptionResponseCode.INVALID_BIRTH_YEAR.detailCode))

                testChildFixture.findAllByGuardianId(guardianId).size shouldBe 0
            }

            test("출생연도가 누락되면 400을 반환한다") {
                val guardianId = "guardian-${uniqueSuffix()}"
                testUserFixture.saveUser(userEntity(guardianId))
                val token = testJwtTokenFixture.generateAccessToken(guardianId)

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .body(mapOf("nickname" to "토토"))
                    .`when`()
                    .post("/children")
                    .then()
                    .statusCode(400)
                    .body("success", equalTo(false))
                    .body("code", equalTo(400))

                testChildFixture.findAllByGuardianId(guardianId).size shouldBe 0
            }
        }
    }

    context("getChildren") {
        context("성공") {
            test("현재 보호자의 아이만 반환하고 타 보호자 아이는 섞이지 않는다") {
                val guardianId = "guardian-${uniqueSuffix()}"
                val otherGuardianId = "other-${uniqueSuffix()}"
                testUserFixture.saveUser(userEntity(guardianId))
                testUserFixture.saveUser(userEntity(otherGuardianId))
                val token = testJwtTokenFixture.generateAccessToken(guardianId)
                testChildFixture.saveChild(childEntity("mine-1-$guardianId", guardianId, "토토"))
                testChildFixture.saveChild(childEntity("mine-2-$guardianId", guardianId, "코코"))
                testChildFixture.saveChild(childEntity("theirs-$otherGuardianId", otherGuardianId, "루루"))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .`when`()
                    .get("/children")
                    .then()
                    .statusCode(200)
                    .body("success", equalTo(true))
                    .body("code", equalTo(200))
                    .body("message", equalTo(SuccessResponseCode.OK.message))
                    .body("data", hasSize<Any>(2))
                    .body("data.childId", containsInAnyOrder("mine-1-$guardianId", "mine-2-$guardianId"))
                    .body("data.birthYear", containsInAnyOrder(2019, 2019))
            }
        }
    }

    context("getChild") {
        context("성공") {
            test("본인 아이를 단건 조회하면 200과 아이 정보를 반환한다") {
                val guardianId = "guardian-${uniqueSuffix()}"
                testUserFixture.saveUser(userEntity(guardianId))
                val token = testJwtTokenFixture.generateAccessToken(guardianId)
                val childId = "mine-$guardianId"
                testChildFixture.saveChild(childEntity(childId, guardianId, "토토"))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .`when`()
                    .get("/children/$childId")
                    .then()
                    .statusCode(200)
                    .body("success", equalTo(true))
                    .body("data.childId", equalTo(childId))
                    .body("data.nickname", equalTo("토토"))
                    .body("data.birthYear", equalTo(2019))
            }
        }
        context("예외케이스") {
            test("타 보호자의 아이 id로 조회하면 404와 NOT_FOUND_CHILD로 은닉한다") {
                val guardianId = "guardian-${uniqueSuffix()}"
                val otherGuardianId = "other-${uniqueSuffix()}"
                testUserFixture.saveUser(userEntity(guardianId))
                testUserFixture.saveUser(userEntity(otherGuardianId))
                val token = testJwtTokenFixture.generateAccessToken(guardianId)
                val otherChildId = "theirs-$otherGuardianId"
                testChildFixture.saveChild(childEntity(otherChildId, otherGuardianId, "루루"))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .`when`()
                    .get("/children/$otherChildId")
                    .then()
                    .statusCode(404)
                    .body("success", equalTo(false))
                    .body("code", equalTo(404))
                    .body("detailCode", equalTo(ExceptionResponseCode.NOT_FOUND_CHILD.detailCode))
                    .body("message", equalTo("아이가 존재하지 않습니다."))
            }

            test("존재하지 않는 아이 id로 조회하면 404와 NOT_FOUND_CHILD를 반환한다") {
                val guardianId = "guardian-${uniqueSuffix()}"
                testUserFixture.saveUser(userEntity(guardianId))
                val token = testJwtTokenFixture.generateAccessToken(guardianId)

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .`when`()
                    .get("/children/non-existent-${uniqueSuffix()}")
                    .then()
                    .statusCode(404)
                    .body("detailCode", equalTo(ExceptionResponseCode.NOT_FOUND_CHILD.detailCode))
            }
        }
    }
})
