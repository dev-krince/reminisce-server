package com.krince.reminisce.infra.adapter.`in`.controller

import com.krince.reminisce.infra.adapter.out.persistence.user.entity.UserOrmEntity
import com.krince.reminisce.shared.response.ExceptionResponseCode
import com.krince.reminisce.shared.response.SuccessResponseCode
import com.krince.reminisce.testutil.TestConfig
import com.krince.reminisce.testutil.fixture.TestJwtTokenFixture
import com.krince.reminisce.testutil.fixture.TestUserFixture
import io.kotest.common.ExperimentalKotest
import io.kotest.core.annotation.DisplayName
import io.kotest.core.spec.style.FunSpec
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.parsing.Parser
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasKey
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
@DisplayName("UserControllerImpl 통합테스트")
class UserControllerImplTest(
    @param:LocalServerPort private val port: Int,
    private val testUserFixture: TestUserFixture,
    private val testJwtTokenFixture: TestJwtTokenFixture,
) : FunSpec({

    beforeSpec {
        RestAssured.port = port
        RestAssured.basePath = "/api"
        RestAssured.defaultParser = Parser.JSON
    }

    beforeTest {
        testUserFixture.deleteAllBatch()
    }

    context("getUser") {
        context("성공") {
            test("유효한 토큰으로 회원을 조회하면 200과 회원 정보를 반환한다") {
                val userId = "user-${System.currentTimeMillis()}-${Thread.currentThread().id}"
                val loginId = "testUser${System.currentTimeMillis()}"
                val savedUser = testUserFixture.saveUser(
                    UserOrmEntity(userId = userId, loginId = loginId, role = "ROLE_USER")
                )
                val token = testJwtTokenFixture.generateAccessToken(savedUser.userId, savedUser.role)

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .`when`()
                    .get("/users/${savedUser.userId}")
                    .then()
                    .statusCode(200)
                    .body("success", equalTo(true))
                    .body("code", equalTo(200))
                    .body("message", equalTo(SuccessResponseCode.OK.message))
                    .body("data", hasKey("id"))
                    .body("data", hasKey("loginId"))
                    .body("data", hasKey("role"))
                    .body("data", hasKey("createdDate"))
                    .body("data", hasKey("modifiedDate"))
                    .body("data.id", equalTo(userId))
                    .body("data.loginId", equalTo(loginId))
                    .body("data.role", equalTo("ROLE_USER"))
            }
        }
        context("예외케이스") {
            test("토큰이 없으면 401과 EMPTY_TOKEN을 반환한다") {
                val userId = "user-${System.currentTimeMillis()}"
                testUserFixture.saveUser(UserOrmEntity(userId = userId, loginId = "login$userId", role = "ROLE_USER"))

                RestAssured.given()
                    .contentType(ContentType.JSON)
                    .`when`()
                    .get("/users/$userId")
                    .then()
                    .statusCode(401)
                    .body("success", equalTo(false))
                    .body("code", equalTo(401))
                    .body("detailCode", equalTo(ExceptionResponseCode.EMPTY_TOKEN.detailCode))
                    .body("message", equalTo("토큰이 없습니다."))
            }
            test("유효하지 않은 토큰이면 401과 INVALID_TOKEN을 반환한다") {
                val userId = "user-${System.currentTimeMillis()}"
                testUserFixture.saveUser(UserOrmEntity(userId = userId, loginId = "login$userId", role = "ROLE_USER"))

                RestAssured.given()
                    .header("Authorization", "Bearer invalid.token.here")
                    .contentType(ContentType.JSON)
                    .`when`()
                    .get("/users/$userId")
                    .then()
                    .statusCode(401)
                    .body("success", equalTo(false))
                    .body("detailCode", equalTo(ExceptionResponseCode.INVALID_TOKEN.detailCode))
                    .body("message", equalTo("유효하지 않은 토큰입니다."))
            }
            test("존재하지 않는 회원 ID로 조회하면 404와 NOT_FOUND_USER를 반환한다") {
                val existingUser = testUserFixture.saveUser(
                    UserOrmEntity(userId = "auth-user-${System.currentTimeMillis()}", loginId = "authLogin", role = "ROLE_USER")
                )
                val token = testJwtTokenFixture.generateAccessToken(existingUser.userId, existingUser.role)
                val nonExistentUserId = "non-existent-${System.currentTimeMillis()}"

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .`when`()
                    .get("/users/$nonExistentUserId")
                    .then()
                    .statusCode(404)
                    .body("success", equalTo(false))
                    .body("code", equalTo(404))
                    .body("detailCode", equalTo(ExceptionResponseCode.NOT_FOUND_USER.detailCode))
                    .body("message", equalTo("회원이 존재하지 않습니다."))
            }
        }
    }
})
