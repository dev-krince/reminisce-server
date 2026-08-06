package com.krince.reminisce.application.service.notice

import com.krince.reminisce.application.port.`in`.notice.command.GetNoticeCommand
import com.krince.reminisce.application.port.out.notice.LoadNoticePort
import com.krince.reminisce.domain.model.notice.Notice
import com.krince.reminisce.domain.model.notice.vo.NoticeId
import com.krince.reminisce.domain.model.notice.vo.NoticeStatus
import com.krince.reminisce.shared.exception.NotFoundException
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDateTime

@Tags("test", "unitTest")
@DisplayName("NoticeApplicationService 단위테스트")
class NoticeApplicationServiceTest : FunSpec({

    val loadNoticePort = mockk<LoadNoticePort>()
    val service = NoticeApplicationService(loadNoticePort)

    beforeEach { clearAllMocks() }

    fun notice(
        noticeId: String,
        title: String,
        content: String,
        status: NoticeStatus = NoticeStatus.PUBLISHED,
        createdAt: LocalDateTime? = null,
    ): Notice = Notice(
        noticeId = NoticeId(noticeId),
        title = title,
        content = content,
        status = status,
        createdAt = createdAt,
    )

    context("GetNoticesUseCase") {
        context("성공") {
            test("게시된 공지를 최근순으로 요약 매핑해 반환한다") {
                val newer = notice("notice-2", "두번째 공지", "내용2", createdAt = LocalDateTime.of(2026, 8, 2, 10, 0))
                val older = notice("notice-1", "첫번째 공지", "내용1", createdAt = LocalDateTime.of(2026, 8, 1, 10, 0))
                every { loadNoticePort.findAllPublished() } returns listOf(newer, older)

                val results = service.execute()

                results shouldHaveSize 2
                results[0].noticeId shouldBe "notice-2"
                results[0].title shouldBe "두번째 공지"
                results[1].noticeId shouldBe "notice-1"
            }

            test("게시된 공지가 없으면 빈 목록을 반환한다") {
                every { loadNoticePort.findAllPublished() } returns emptyList()

                val results = service.execute()

                results shouldHaveSize 0
            }
        }
    }

    context("GetNoticeUseCase") {
        context("성공") {
            test("게시된 공지 상세를 content 포함해 반환한다") {
                val noticeId = "notice-1"
                val createdAt = LocalDateTime.of(2026, 8, 1, 10, 0)
                every { loadNoticePort.findByIdPublished(NoticeId(noticeId)) } returns
                    notice(noticeId, "공지 제목", "공지 내용", createdAt = createdAt)

                val result = service.execute(GetNoticeCommand(noticeId = noticeId))

                result.noticeId shouldBe noticeId
                result.title shouldBe "공지 제목"
                result.content shouldBe "공지 내용"
                result.createdAt shouldBe createdAt
            }
        }
        context("실패") {
            test("게시된 공지가 없으면 NOT_FOUND로 NotFoundException을 던진다") {
                every { loadNoticePort.findByIdPublished(NoticeId("unknown")) } returns null

                val exception = shouldThrow<NotFoundException> {
                    service.execute(GetNoticeCommand(noticeId = "unknown"))
                }

                exception.exceptionResponseCode shouldBe NOT_FOUND
            }
        }
    }
})
