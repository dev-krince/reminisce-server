package com.krince.reminisce.infra.adapter.`in`.controller

import com.krince.reminisce.infra.adapter.`in`.dto.profileinterview.request.StartProfileInterviewRequest
import com.krince.reminisce.infra.adapter.`in`.dto.profileinterview.response.ProfileInterviewResponse
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
import com.krince.reminisce.shared.response.SuccessResponse
import com.krince.reminisce.shared.response.SuccessResponseCode.CREATED
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.RequestBody

@Tag(name = "프로필 인터뷰(Profile Interviews)")
interface ProfileInterviewController {

    @Operation(
        summary = "프로필 인터뷰 시작",
        description = "큐미가 아이와 나누는 맞춤 추천 인터뷰를 시작합니다. 진행 중 인터뷰가 있으면 새로 만들지 않고 그 인터뷰와 마지막 큐미 질문을 반환합니다. 첫 시작 시 큐미의 첫 질문 텍스트·음성을 함께 반환합니다.",
    )
    @SwaggerSuccessResponse(responseCode = CREATED, description = "프로필 인터뷰 시작 성공 (기존 진행분 재개 시 200)")
    @SwaggerExceptionResponse(
        examples = [
            ExceptionExample(code = INVALID_DTO_PARAMETER, name = "요청 값 오류", message = "요청 값이 올바르지 않습니다.(dto 검증 오류)", description = "요청 바디 검증에 실패한 경우"),
            ExceptionExample(code = EMPTY_TOKEN, name = "토큰 없음", message = "토큰이 없습니다.", description = "인증 토큰이 제공되지 않은 경우"),
            ExceptionExample(code = INVALID_TOKEN, name = "유효하지 않은 토큰", message = "유효하지 않은 토큰입니다.", description = "토큰이 유효하지 않거나 서명이 잘못된 경우"),
            ExceptionExample(code = EXPIRED_TOKEN, name = "만료된 토큰", message = "만료된 토큰입니다.", description = "토큰의 유효기간이 만료된 경우"),
            ExceptionExample(code = NOT_FOUND_CHILD, name = "아이 없음", message = "아이가 존재하지 않습니다.", description = "아이가 없거나 다른 보호자의 아이인 경우"),
            ExceptionExample(code = CONSENT_REQUIRED, name = "동의 없음", message = "법정대리인 동의가 없어 세션을 시작할 수 없습니다.", description = "아이의 활성 동의가 없는 경우"),
            ExceptionExample(code = INTERNAL_SERVER_ERROR, name = "서버 오류", message = "서버 에러입니다. 개발자에게 문의해주세요.", description = "예상치 못한 서버 오류가 발생한 경우"),
        ]
    )
    fun startProfileInterview(
        @Valid @RequestBody request: StartProfileInterviewRequest,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<SuccessResponse<ProfileInterviewResponse>>
}
