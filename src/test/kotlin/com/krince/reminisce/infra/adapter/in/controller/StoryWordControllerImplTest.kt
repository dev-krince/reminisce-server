package com.krince.reminisce.infra.adapter.`in`.controller

import com.krince.reminisce.infra.adapter.out.persistence.child.entity.ChildOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.user.entity.UserOrmEntity
import com.krince.reminisce.shared.response.ExceptionResponseCode
import com.krince.reminisce.testutil.TestConfig
import com.krince.reminisce.testutil.fixture.TestChildFixture
import com.krince.reminisce.testutil.fixture.TestJwtTokenFixture
import com.krince.reminisce.testutil.fixture.TestUserFixture
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.restassured.RestAssured
import io.restassured.parsing.Parser
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
@Tags("test", "integrationTest")
@DisplayName("StoryWordControllerImpl 통합테스트")
class StoryWordControllerImplTest(
    @param:LocalServerPort private val port: Int,
    private val testUserFixture: TestUserFixture,
    private val testJwtTokenFixture: TestJwtTokenFixture,
    private val testChildFixture: TestChildFixture,
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

    beforeSpec {
        RestAssured.port = port
        RestAssured.basePath = "/api"
        RestAssured.defaultParser = Parser.JSON
    }

    context("getStoryWords") {
        context("성공") {
            test("소유한 아이로 조회하면 방귀 뀌는 며느리 그룹과 고정 단어 6개를 반환한다") {
                val (token, guardianId) = authorizedTokenWithGuardian()
                val childId = "child-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, guardianId))

                RestAssured.given()
                    .header("Authorization", token)
                    .`when`()
                    .get("/children/$childId/story-words")
                    .then()
                    .statusCode(200)
                    .body("success", equalTo(true))
                    .body("data", hasSize<Any>(1))
                    .body("data[0].storyId", equalTo("s_banggui_daughter_in_law_001"))
                    .body("data[0].storyTitle", equalTo("방귀 뀌는 며느리"))
                    .body("data[0].totalWords", equalTo(6))
                    .body("data[0].words", hasSize<Any>(6))
                    .body("data[0].words[0].word", equalTo("방귀를 참다"))
                    .body("data[0].words[0].meaning", equalTo("나오려는 방귀를 뀌지 않고 꾹 견디는 것"))
                    .body("data[0].words[0].imageUrl", equalTo("/files/banggui-word-01.png"))
                    .body("data[0].words[0].audioUrl", notNullValue())
                    .body("data[0].words[5].word", equalTo("사과하다"))
                    .body("data[0].words[5].meaning", equalTo("잘못을 인정하고 미안한 마음을 전하는 것"))
                    .body("data[0].words[5].imageUrl", equalTo("/files/banggui-word-06.png"))
                    .body("data[0].words[5].audioUrl", notNullValue())
            }
        }

        context("예외케이스") {
            test("남의 아이 childId로 조회하면 404와 NOT_FOUND를 반환한다") {
                val (token, _) = authorizedTokenWithGuardian()
                val otherGuardianId = "guardian-${uniqueSuffix()}"
                testUserFixture.saveUser(userEntity(otherGuardianId))
                val childId = "child-${uniqueSuffix()}"
                testChildFixture.saveChild(childEntity(childId, otherGuardianId))

                RestAssured.given()
                    .header("Authorization", token)
                    .`when`()
                    .get("/children/$childId/story-words")
                    .then()
                    .statusCode(404)
                    .body("detailCode", equalTo(ExceptionResponseCode.NOT_FOUND.detailCode))
            }

            test("토큰 없이 조회하면 401과 EMPTY_TOKEN을 반환한다") {
                RestAssured.given()
                    .`when`()
                    .get("/children/child-any/story-words")
                    .then()
                    .statusCode(401)
                    .body("detailCode", equalTo(ExceptionResponseCode.EMPTY_TOKEN.detailCode))
            }
        }
    }
})
