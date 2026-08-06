package com.krince.reminisce.infra.adapter.`in`.controller

import com.krince.reminisce.infra.adapter.`in`.dto.notice.response.NoticeDetailResponse
import com.krince.reminisce.infra.adapter.`in`.dto.notice.response.NoticeSummaryResponse
import com.krince.reminisce.infra.swagger.ExceptionExample
import com.krince.reminisce.infra.swagger.SwaggerExceptionResponse
import com.krince.reminisce.infra.swagger.SwaggerSuccessResponse
import com.krince.reminisce.shared.response.ExceptionResponseCode.EMPTY_TOKEN
import com.krince.reminisce.shared.response.ExceptionResponseCode.EXPIRED_TOKEN
import com.krince.reminisce.shared.response.ExceptionResponseCode.INTERNAL_SERVER_ERROR
import com.krince.reminisce.shared.response.ExceptionResponseCode.INVALID_TOKEN
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND
import com.krince.reminisce.shared.response.SuccessResponse
import com.krince.reminisce.shared.response.SuccessResponseCode.OK
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable

@Tag(name = "공지사항(Notices)")
interface NoticeController {

    @Operation(summary = "공지사항 목록 조회", description = "게시된 공지사항 목록을 최근순으로 조회합니다.")
    @SwaggerSuccessResponse(responseCode = OK, description = "공지사항 목록 조회 성공")
    @SwaggerExceptionResponse(
        examples = [
            ExceptionExample(code = EMPTY_TOKEN, name = "토큰 없음", message = "토큰이 없습니다.", description = "인증 토큰이 제공되지 않은 경우"),
            ExceptionExample(code = INVALID_TOKEN, name = "유효하지 않은 토큰", message = "유효하지 않은 토큰입니다.", description = "토큰이 유효하지 않거나 서명이 잘못된 경우"),
            ExceptionExample(code = EXPIRED_TOKEN, name = "만료된 토큰", message = "만료된 토큰입니다.", description = "토큰의 유효기간이 만료된 경우"),
            ExceptionExample(code = INTERNAL_SERVER_ERROR, name = "서버 오류", message = "서버 에러입니다. 개발자에게 문의해주세요.", description = "예상치 못한 서버 오류가 발생한 경우"),
        ]
    )
    fun getNotices(): ResponseEntity<SuccessResponse<List<NoticeSummaryResponse>>>

    @Operation(summary = "공지사항 상세 조회", description = "게시된 공지사항의 상세 정보를 조회합니다. 미게시 또는 존재하지 않는 공지는 404로 은닉합니다.")
    @SwaggerSuccessResponse(responseCode = OK, description = "공지사항 상세 조회 성공")
    @SwaggerExceptionResponse(
        examples = [
            ExceptionExample(code = EMPTY_TOKEN, name = "토큰 없음", message = "토큰이 없습니다.", description = "인증 토큰이 제공되지 않은 경우"),
            ExceptionExample(code = INVALID_TOKEN, name = "유효하지 않은 토큰", message = "유효하지 않은 토큰입니다.", description = "토큰이 유효하지 않거나 서명이 잘못된 경우"),
            ExceptionExample(code = EXPIRED_TOKEN, name = "만료된 토큰", message = "만료된 토큰입니다.", description = "토큰의 유효기간이 만료된 경우"),
            ExceptionExample(code = NOT_FOUND, name = "공지사항 없음", message = "리소스가 존재하지 않습니다.", description = "공지사항이 없거나 게시 상태가 아닌 경우"),
            ExceptionExample(code = INTERNAL_SERVER_ERROR, name = "서버 오류", message = "서버 에러입니다. 개발자에게 문의해주세요.", description = "예상치 못한 서버 오류가 발생한 경우"),
        ]
    )
    fun getNotice(
        @Parameter(description = "공지사항 고유 식별자", example = "notice-001", required = true)
        @PathVariable
        noticeId: String,
    ): ResponseEntity<SuccessResponse<NoticeDetailResponse>>
}
