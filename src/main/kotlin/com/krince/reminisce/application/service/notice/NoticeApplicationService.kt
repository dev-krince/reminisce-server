package com.krince.reminisce.application.service.notice

import com.krince.reminisce.application.port.`in`.notice.command.GetNoticeCommand
import com.krince.reminisce.application.port.`in`.notice.result.NoticeDetailResult
import com.krince.reminisce.application.port.`in`.notice.result.NoticeSummaryResult
import com.krince.reminisce.application.port.`in`.notice.usecase.GetNoticeUseCase
import com.krince.reminisce.application.port.`in`.notice.usecase.GetNoticesUseCase
import com.krince.reminisce.application.port.out.notice.LoadNoticePort
import com.krince.reminisce.domain.model.notice.Notice
import com.krince.reminisce.domain.model.notice.vo.NoticeId
import com.krince.reminisce.shared.exception.NotFoundException
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class NoticeApplicationService(
    private val loadNoticePort: LoadNoticePort,
) : GetNoticesUseCase, GetNoticeUseCase {

    @Transactional(readOnly = true)
    override fun execute(): List<NoticeSummaryResult> =
        loadNoticePort.findAllPublished().map { NoticeSummaryResult.from(it) }

    @Transactional(readOnly = true)
    override fun execute(command: GetNoticeCommand): NoticeDetailResult {
        val notice: Notice = loadNoticePort.findByIdPublished(NoticeId(command.noticeId))
            ?: throw NotFoundException(NOT_FOUND, NOT_FOUND.message)

        return NoticeDetailResult.from(notice)
    }
}
