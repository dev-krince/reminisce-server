package com.krince.reminisce.infra.adapter.`in`.controller

import com.krince.reminisce.application.port.`in`.notice.command.GetNoticeCommand
import com.krince.reminisce.application.port.`in`.notice.result.NoticeDetailResult
import com.krince.reminisce.application.port.`in`.notice.result.NoticeSummaryResult
import com.krince.reminisce.application.port.`in`.notice.usecase.GetNoticeUseCase
import com.krince.reminisce.application.port.`in`.notice.usecase.GetNoticesUseCase
import com.krince.reminisce.infra.adapter.`in`.dto.notice.response.NoticeDetailResponse
import com.krince.reminisce.infra.adapter.`in`.dto.notice.response.NoticeSummaryResponse
import com.krince.reminisce.infra.adapter.`in`.dto.notice.response.noticeDetailResponse
import com.krince.reminisce.infra.adapter.`in`.dto.notice.response.noticeSummaryResponse
import com.krince.reminisce.shared.response.SuccessResponse
import com.krince.reminisce.shared.response.SuccessResponseCode.OK
import com.krince.reminisce.shared.response.successResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/notices")
class NoticeControllerImpl(
    private val getNoticesUseCase: GetNoticesUseCase,
    private val getNoticeUseCase: GetNoticeUseCase,
) : NoticeController {

    @GetMapping
    override fun getNotices(): ResponseEntity<SuccessResponse<List<NoticeSummaryResponse>>> {
        val results: List<NoticeSummaryResult> = getNoticesUseCase.execute()
        val response: List<NoticeSummaryResponse> = results.map { noticeSummaryResponse(result = it) }
        val responseBody: SuccessResponse<List<NoticeSummaryResponse>> = successResponse(responseCode = OK, data = response)

        return ResponseEntity.status(responseBody.code).body(responseBody)
    }

    @GetMapping("/{noticeId}")
    override fun getNotice(
        @PathVariable noticeId: String,
    ): ResponseEntity<SuccessResponse<NoticeDetailResponse>> {
        val command = GetNoticeCommand(noticeId = noticeId)
        val result: NoticeDetailResult = getNoticeUseCase.execute(command)
        val response: NoticeDetailResponse = noticeDetailResponse(result = result)
        val responseBody: SuccessResponse<NoticeDetailResponse> = successResponse(responseCode = OK, data = response)

        return ResponseEntity.status(responseBody.code).body(responseBody)
    }
}
