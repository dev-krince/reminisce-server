package com.krince.reminisce.infra.adapter.`in`.controller

import com.krince.reminisce.shared.response.ExceptionResponseCode
import com.krince.reminisce.testutil.TestConfig
import com.krince.reminisce.testutil.fixture.TestUserFixture
import io.kotest.common.ExperimentalKotest
import io.kotest.core.annotation.DisplayName
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.parsing.Parser
import org.hamcrest.Matchers.equalTo
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.test.context.ActiveProfiles

@OptIn(ExperimentalKotest::class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("localtest")
@Import(TestConfig::class)
@io.kotest.core.annotation.Tags("test", "integrationTest")
@DisplayName("이메일 회원가입 통합테스트")
class AuthSignUpControllerImplTest(
    @param:LocalServerPort private val port: Int,
    private val testUserFixture: TestUserFixture,
    private val redisTemplate: StringRedisTemplate,
) : FunSpec({

    fun sendCode(email: String) {
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(mapOf("email" to email))
            .`when`()
            .post("/users/email-verifications")
            .then()
            .statusCode(204)
    }

    fun storedCode(email: String): String? = redisTemplate.opsForValue().get("email:verification:$email")

    fun confirmCode(email: String, code: String) {
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(mapOf("email" to email, "code" to code))
            .`when`()
            .post("/users/email-verifications/confirm")
            .then()
            .statusCode(204)
    }

    beforeSpec {
        RestAssured.port = port
        RestAssured.basePath = "/api"
        RestAssured.defaultParser = Parser.JSON
    }

    beforeTest {
        testUserFixture.deleteAllBatch()
        redisTemplate.connectionFactory?.connection?.serverCommands()?.flushAll()
    }

    context("이메일 회원가입 정상 흐름") {
        test("코드 발송 → 확인 → 가입하면 201과 회원이 생성되고 비밀번호는 BCrypt 해시로 저장된다") {
            val email = "signup${System.currentTimeMillis()}@example.com"
            val rawPassword = "Password1!"

            sendCode(email)
            val code = storedCode(email)
            code.shouldNotBeNull()
            confirmCode(email, code)

            RestAssured.given()
                .contentType(ContentType.JSON)
                .body(mapOf("email" to email, "password" to rawPassword, "nickname" to "홍길동"))
                .`when`()
                .post("/users")
                .then()
                .statusCode(201)
                .body("success", equalTo(true))
                .body("code", equalTo(201))
                .body("data.email", equalTo(email))
                .body("data.nickname", equalTo("홍길동"))
                .body("data.role", equalTo("ROLE_USER"))

            val savedUser = testUserFixture.findByEmail(email)
            savedUser.shouldNotBeNull()
            savedUser.provider shouldBe "LOCAL"
            savedUser.role shouldBe "ROLE_USER"
            savedUser.password shouldStartWith "\$2"
            (savedUser.password == rawPassword) shouldBe false
        }
    }

    context("가입 거부 케이스") {
        test("이메일 인증 없이 가입하면 400과 EMAIL_NOT_VERIFIED를 반환한다") {
            val email = "unverified${System.currentTimeMillis()}@example.com"

            RestAssured.given()
                .contentType(ContentType.JSON)
                .body(mapOf("email" to email, "password" to "Password1!", "nickname" to "홍길동"))
                .`when`()
                .post("/users")
                .then()
                .statusCode(400)
                .body("success", equalTo(false))
                .body("detailCode", equalTo(ExceptionResponseCode.EMAIL_NOT_VERIFIED.detailCode))

            testUserFixture.findByEmail(email) shouldBe null
        }
        test("비밀번호 정책 위반이면 400과 INVALID_PASSWORD_FORMAT을 반환한다") {
            val email = "weakpw${System.currentTimeMillis()}@example.com"

            sendCode(email)
            val code = storedCode(email)
            code.shouldNotBeNull()
            confirmCode(email, code)

            RestAssured.given()
                .contentType(ContentType.JSON)
                .body(mapOf("email" to email, "password" to "weakpass", "nickname" to "홍길동"))
                .`when`()
                .post("/users")
                .then()
                .statusCode(400)
                .body("detailCode", equalTo(ExceptionResponseCode.INVALID_PASSWORD_FORMAT.detailCode))

            testUserFixture.findByEmail(email) shouldBe null
        }
        test("이메일 형식이 올바르지 않으면 400을 반환한다") {
            RestAssured.given()
                .contentType(ContentType.JSON)
                .body(mapOf("email" to "not-an-email", "password" to "Password1!", "nickname" to "홍길동"))
                .`when`()
                .post("/users")
                .then()
                .statusCode(400)
                .body("success", equalTo(false))
        }
    }

    context("인증코드 확인 케이스") {
        test("코드가 일치하지 않으면 400과 INVALID_VERIFICATION_CODE를 반환한다") {
            val email = "wrongcode${System.currentTimeMillis()}@example.com"

            sendCode(email)

            RestAssured.given()
                .contentType(ContentType.JSON)
                .body(mapOf("email" to email, "code" to "000000"))
                .`when`()
                .post("/users/email-verifications/confirm")
                .then()
                .statusCode(400)
                .body("detailCode", equalTo(ExceptionResponseCode.INVALID_VERIFICATION_CODE.detailCode))
        }
        test("코드가 존재하지 않으면(만료) 400과 EXPIRED_VERIFICATION_CODE를 반환한다") {
            val email = "expired${System.currentTimeMillis()}@example.com"

            RestAssured.given()
                .contentType(ContentType.JSON)
                .body(mapOf("email" to email, "code" to "123456"))
                .`when`()
                .post("/users/email-verifications/confirm")
                .then()
                .statusCode(400)
                .body("detailCode", equalTo(ExceptionResponseCode.EXPIRED_VERIFICATION_CODE.detailCode))
        }
    }

    context("이메일 중복 케이스") {
        test("이미 가입된 이메일로 발송하면 409와 DUPLICATE_EMAIL을 반환한다") {
            val email = "dup${System.currentTimeMillis()}@example.com"

            sendCode(email)
            val code = storedCode(email)
            code.shouldNotBeNull()
            confirmCode(email, code)

            RestAssured.given()
                .contentType(ContentType.JSON)
                .body(mapOf("email" to email, "password" to "Password1!", "nickname" to "홍길동"))
                .`when`()
                .post("/users")
                .then()
                .statusCode(201)

            RestAssured.given()
                .contentType(ContentType.JSON)
                .body(mapOf("email" to email))
                .`when`()
                .post("/users/email-verifications")
                .then()
                .statusCode(409)
                .body("detailCode", equalTo(ExceptionResponseCode.DUPLICATE_EMAIL.detailCode))
        }
        test("이미 가입된 이메일로 다시 가입하면 409와 DUPLICATE_EMAIL을 반환한다") {
            val email = "dupsignup${System.currentTimeMillis()}@example.com"

            sendCode(email)
            val code = storedCode(email)
            code.shouldNotBeNull()
            confirmCode(email, code)

            RestAssured.given()
                .contentType(ContentType.JSON)
                .body(mapOf("email" to email, "password" to "Password1!", "nickname" to "홍길동"))
                .`when`()
                .post("/users")
                .then()
                .statusCode(201)

            RestAssured.given()
                .contentType(ContentType.JSON)
                .body(mapOf("email" to email, "password" to "Password1!", "nickname" to "홍길동"))
                .`when`()
                .post("/users")
                .then()
                .statusCode(409)
                .body("detailCode", equalTo(ExceptionResponseCode.DUPLICATE_EMAIL.detailCode))
        }
    }
})
