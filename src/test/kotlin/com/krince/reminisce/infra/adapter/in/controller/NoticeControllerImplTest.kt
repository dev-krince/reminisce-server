package com.krince.reminisce.infra.adapter.`in`.controller

import com.krince.reminisce.domain.model.notice.vo.NoticeStatus
import com.krince.reminisce.infra.adapter.out.persistence.notice.entity.NoticeOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.user.entity.UserOrmEntity
import com.krince.reminisce.shared.response.ExceptionResponseCode
import com.krince.reminisce.shared.response.SuccessResponseCode
import com.krince.reminisce.testutil.TestConfig
import com.krince.reminisce.testutil.fixture.TestJwtTokenFixture
import com.krince.reminisce.testutil.fixture.TestNoticeFixture
import com.krince.reminisce.testutil.fixture.TestUserFixture
import io.kotest.core.annotation.DisplayName
import io.kotest.core.spec.style.FunSpec
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.parsing.Parser
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.notNullValue
import org.hamcrest.Matchers.nullValue
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("localtest")
@Import(TestConfig::class)
@io.kotest.core.annotation.Tags("test", "integrationTest")
@DisplayName("NoticeControllerImpl 통합테스트")
class NoticeControllerImplTest(
    @param:LocalServerPort private val port: Int,
    private val testUserFixture: TestUserFixture,
    private val testNoticeFixture: TestNoticeFixture,
    private val testJwtTokenFixture: TestJwtTokenFixture,
) : FunSpec({

    fun uniqueSuffix(): String = "${System.currentTimeMillis()}-${System.nanoTime()}"

    fun userEntity(userId: String): UserOrmEntity =
        UserOrmEntity(
            userId = userId,
            email = "$userId@example.com",
            password = "\$2a\$10\$hashedvaluehashedvaluehashedvalue",
            nickname = "홍길동",
            provider = "LOCAL",
            role = "ROLE_USER",
        )

    fun authorizedToken(): String {
        val guardianId = "guardian-${uniqueSuffix()}"
        testUserFixture.saveUser(userEntity(guardianId))

        return testJwtTokenFixture.generateAccessToken(guardianId)
    }

    fun noticeEntity(
        noticeId: String,
        title: String = "제목-$noticeId",
        content: String = "내용-$noticeId",
        status: String = NoticeStatus.PUBLISHED.name,
    ): NoticeOrmEntity = NoticeOrmEntity(
        noticeId = noticeId,
        title = title,
        content = content,
        status = status,
    )

    beforeSpec {
        RestAssured.port = port
        RestAssured.basePath = "/api"
        RestAssured.defaultParser = Parser.JSON
    }

    beforeTest {
        testNoticeFixture.deleteAllBatch()
        testUserFixture.deleteAllBatch()
    }

    context("getNotices") {
        context("성공") {
            test("게시된 공지만 최근순으로 반환하고 초안은 제외하며 응답에 content가 없다") {
                val token = authorizedToken()
                val olderPublishedId = "notice-older-${uniqueSuffix()}"
                val newerPublishedId = "notice-newer-${uniqueSuffix()}"
                val draftId = "notice-draft-${uniqueSuffix()}"

                testNoticeFixture.saveNotice(noticeEntity(olderPublishedId))
                Thread.sleep(10)
                testNoticeFixture.saveNotice(noticeEntity(newerPublishedId))
                testNoticeFixture.saveNotice(noticeEntity(draftId, status = NoticeStatus.DRAFT.name))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .`when`()
                    .get("/notices")
                    .then()
                    .statusCode(200)
                    .body("success", equalTo(true))
                    .body("code", equalTo(200))
                    .body("message", equalTo(SuccessResponseCode.OK.message))
                    .body("data", hasSize<Any>(2))
                    .body("data[0].noticeId", equalTo(newerPublishedId))
                    .body("data[1].noticeId", equalTo(olderPublishedId))
                    .body("data[0].title", notNullValue())
                    .body("data[0].content", nullValue())
            }
        }
        context("예외케이스") {
            test("토큰이 없으면 401과 EMPTY_TOKEN을 반환한다") {
                RestAssured.given()
                    .contentType(ContentType.JSON)
                    .`when`()
                    .get("/notices")
                    .then()
                    .statusCode(401)
                    .body("success", equalTo(false))
                    .body("code", equalTo(401))
                    .body("detailCode", equalTo(ExceptionResponseCode.EMPTY_TOKEN.detailCode))
                    .body("message", equalTo("토큰이 없습니다."))
            }
        }
    }

    context("getNotice") {
        context("성공") {
            test("게시된 공지 상세를 content 포함해 반환한다") {
                val token = authorizedToken()
                val noticeId = "notice-detail-${uniqueSuffix()}"
                testNoticeFixture.saveNotice(noticeEntity(noticeId, title = "상세 제목", content = "상세 내용"))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .`when`()
                    .get("/notices/$noticeId")
                    .then()
                    .statusCode(200)
                    .body("success", equalTo(true))
                    .body("code", equalTo(200))
                    .body("message", equalTo(SuccessResponseCode.OK.message))
                    .body("data.noticeId", equalTo(noticeId))
                    .body("data.title", equalTo("상세 제목"))
                    .body("data.content", equalTo("상세 내용"))
            }
        }
        context("예외케이스") {
            test("초안 공지를 조회하면 404 NOT_FOUND로 은닉한다") {
                val token = authorizedToken()
                val draftId = "notice-draft-${uniqueSuffix()}"
                testNoticeFixture.saveNotice(noticeEntity(draftId, status = NoticeStatus.DRAFT.name))

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .`when`()
                    .get("/notices/$draftId")
                    .then()
                    .statusCode(404)
                    .body("success", equalTo(false))
                    .body("code", equalTo(404))
                    .body("detailCode", equalTo(ExceptionResponseCode.NOT_FOUND.detailCode))
            }

            test("존재하지 않는 공지를 조회하면 404 NOT_FOUND로 은닉한다") {
                val token = authorizedToken()

                RestAssured.given()
                    .header("Authorization", token)
                    .contentType(ContentType.JSON)
                    .`when`()
                    .get("/notices/unknown-${uniqueSuffix()}")
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
                    .get("/notices/any-notice-id")
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
