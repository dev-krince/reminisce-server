package com.krince.reminisce.infra.adapter.`in`.controller

import com.krince.reminisce.infra.adapter.`in`.dto.wordbook.request.SaveWordRequest
import com.krince.reminisce.infra.adapter.`in`.dto.wordbook.response.WordbookResponse
import com.krince.reminisce.infra.security.CustomUserDetails
import com.krince.reminisce.infra.swagger.ExceptionExample
import com.krince.reminisce.infra.swagger.SwaggerExceptionResponse
import com.krince.reminisce.infra.swagger.SwaggerSuccessResponse
import com.krince.reminisce.shared.response.ExceptionResponseCode.EMPTY_TOKEN
import com.krince.reminisce.shared.response.ExceptionResponseCode.EXPIRED_TOKEN
import com.krince.reminisce.shared.response.ExceptionResponseCode.INTERNAL_SERVER_ERROR
import com.krince.reminisce.shared.response.ExceptionResponseCode.INVALID_TOKEN
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND
import com.krince.reminisce.shared.response.SuccessResponse
import com.krince.reminisce.shared.response.SuccessResponseCode.CREATED
import com.krince.reminisce.shared.response.SuccessResponseCode.OK
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody

@Tag(name = "단어장(Wordbook)")
interface WordbookController {

    @Operation(summary = "단어 저장", description = "인증 보호자가 소유한 아이의 단어장에 단어를 저장합니다.")
    @SwaggerSuccessResponse(responseCode = CREATED, description = "단어 저장 성공")
    @SwaggerExceptionResponse(
        examples = [
            ExceptionExample(code = EMPTY_TOKEN, name = "토큰 없음", message = "토큰이 없습니다.", description = "인증 토큰이 제공되지 않은 경우"),
            ExceptionExample(code = INVALID_TOKEN, name = "유효하지 않은 토큰", message = "유효하지 않은 토큰입니다.", description = "토큰이 유효하지 않거나 서명이 잘못된 경우"),
            ExceptionExample(code = EXPIRED_TOKEN, name = "만료된 토큰", message = "만료된 토큰입니다.", description = "토큰의 유효기간이 만료된 경우"),
            ExceptionExample(code = NOT_FOUND, name = "아이 없음", message = "리소스가 존재하지 않습니다.", description = "존재하지 않거나 다른 보호자의 아이인 경우"),
            ExceptionExample(code = INTERNAL_SERVER_ERROR, name = "서버 오류", message = "서버 에러입니다. 개발자에게 문의해주세요.", description = "예상치 못한 서버 오류가 발생한 경우"),
        ]
    )
    fun saveWord(
        @Parameter(description = "아이 고유 식별자", required = true)
        @PathVariable
        childId: String,
        @Valid @RequestBody request: SaveWordRequest,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<SuccessResponse<WordbookResponse>>

    @Operation(summary = "단어장 목록 조회", description = "인증 보호자가 소유한 아이의 단어장을 최근순으로 조회합니다.")
    @SwaggerSuccessResponse(responseCode = OK, description = "단어장 목록 조회 성공")
    @SwaggerExceptionResponse(
        examples = [
            ExceptionExample(code = EMPTY_TOKEN, name = "토큰 없음", message = "토큰이 없습니다.", description = "인증 토큰이 제공되지 않은 경우"),
            ExceptionExample(code = INVALID_TOKEN, name = "유효하지 않은 토큰", message = "유효하지 않은 토큰입니다.", description = "토큰이 유효하지 않거나 서명이 잘못된 경우"),
            ExceptionExample(code = EXPIRED_TOKEN, name = "만료된 토큰", message = "만료된 토큰입니다.", description = "토큰의 유효기간이 만료된 경우"),
            ExceptionExample(code = NOT_FOUND, name = "아이 없음", message = "리소스가 존재하지 않습니다.", description = "존재하지 않거나 다른 보호자의 아이인 경우"),
            ExceptionExample(code = INTERNAL_SERVER_ERROR, name = "서버 오류", message = "서버 에러입니다. 개발자에게 문의해주세요.", description = "예상치 못한 서버 오류가 발생한 경우"),
        ]
    )
    fun getWordbook(
        @Parameter(description = "아이 고유 식별자", required = true)
        @PathVariable
        childId: String,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<SuccessResponse<List<WordbookResponse>>>
}
