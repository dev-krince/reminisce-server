package com.krince.reminisce.infra.adapter.`in`.controller

import com.krince.reminisce.infra.adapter.`in`.dto.speakingsession.request.StartSpeakingSessionRequest
import com.krince.reminisce.infra.adapter.`in`.dto.speakingsession.response.SpeakingSessionResponse
import com.krince.reminisce.infra.security.CustomUserDetails
import com.krince.reminisce.infra.swagger.ExceptionExample
import com.krince.reminisce.infra.swagger.SwaggerExceptionResponse
import com.krince.reminisce.infra.swagger.SwaggerSuccessResponse
import com.krince.reminisce.shared.response.ExceptionResponseCode.CONSENT_REQUIRED
import com.krince.reminisce.shared.response.ExceptionResponseCode.EMPTY_TOKEN
import com.krince.reminisce.shared.response.ExceptionResponseCode.EXPIRED_TOKEN
import com.krince.reminisce.shared.response.ExceptionResponseCode.INTERNAL_SERVER_ERROR
import com.krince.reminisce.shared.response.ExceptionResponseCode.INVALID_DTO_PARAMETER
import com.krince.reminisce.shared.response.ExceptionResponseCode.INVALID_TOKEN
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND_CHILD
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND_STORY
import com.krince.reminisce.shared.response.SuccessResponse
import com.krince.reminisce.shared.response.SuccessResponseCode.CREATED
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.RequestBody

@Tag(name = "말하기 세션(SpeakingSessions)")
interface SpeakingSessionController {

    @Operation(
        summary = "말하기 세션 시작",
        description = "로그인한 보호자가 본인 아이와 공개 이야기로 말하기 세션을 시작합니다. 이미 진행 중인 세션이 있으면 그 세션을 반환합니다.",
    )
    @SwaggerSuccessResponse(responseCode = CREATED, description = "말하기 세션 시작 성공")
    @SwaggerExceptionResponse(
        examples = [
            ExceptionExample(code = INVALID_DTO_PARAMETER, name = "요청 값 오류", message = "요청 값이 올바르지 않습니다.(dto 검증 오류)", description = "요청 바디 검증에 실패한 경우"),
            ExceptionExample(code = EMPTY_TOKEN, name = "토큰 없음", message = "토큰이 없습니다.", description = "인증 토큰이 제공되지 않은 경우"),
            ExceptionExample(code = INVALID_TOKEN, name = "유효하지 않은 토큰", message = "유효하지 않은 토큰입니다.", description = "토큰이 유효하지 않거나 서명이 잘못된 경우"),
            ExceptionExample(code = EXPIRED_TOKEN, name = "만료된 토큰", message = "만료된 토큰입니다.", description = "토큰의 유효기간이 만료된 경우"),
            ExceptionExample(code = NOT_FOUND_CHILD, name = "아이 없음", message = "아이가 존재하지 않습니다.", description = "아이가 없거나 다른 보호자의 아이인 경우"),
            ExceptionExample(code = CONSENT_REQUIRED, name = "동의 필요", message = "법정대리인 동의가 없어 세션을 시작할 수 없습니다.", description = "법정대리인 동의가 없거나 철회된 경우"),
            ExceptionExample(code = NOT_FOUND_STORY, name = "이야기 없음", message = "이야기가 존재하지 않습니다.", description = "이야기가 없거나 미공개인 경우"),
            ExceptionExample(code = INTERNAL_SERVER_ERROR, name = "서버 오류", message = "서버 에러입니다. 개발자에게 문의해주세요.", description = "예상치 못한 서버 오류가 발생한 경우"),
        ]
    )
    fun startSpeakingSession(
        @Valid @RequestBody request: StartSpeakingSessionRequest,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<SuccessResponse<SpeakingSessionResponse>>
}
