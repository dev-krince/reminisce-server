package com.krince.reminisce.infra.adapter.`in`.controller

import com.krince.reminisce.infra.adapter.`in`.dto.child.request.RegisterChildRequest
import com.krince.reminisce.infra.adapter.`in`.dto.child.response.ChildResponse
import com.krince.reminisce.infra.security.CustomUserDetails
import com.krince.reminisce.infra.swagger.ExceptionExample
import com.krince.reminisce.infra.swagger.SwaggerExceptionResponse
import com.krince.reminisce.infra.swagger.SwaggerSuccessResponse
import com.krince.reminisce.shared.response.ExceptionResponseCode.CHILD_LIMIT_EXCEEDED
import com.krince.reminisce.shared.response.ExceptionResponseCode.EMPTY_TOKEN
import com.krince.reminisce.shared.response.ExceptionResponseCode.EXPIRED_TOKEN
import com.krince.reminisce.shared.response.ExceptionResponseCode.INTERNAL_SERVER_ERROR
import com.krince.reminisce.shared.response.ExceptionResponseCode.INVALID_CHILD_NICKNAME_LENGTH
import com.krince.reminisce.shared.response.ExceptionResponseCode.INVALID_DTO_PARAMETER
import com.krince.reminisce.shared.response.ExceptionResponseCode.INVALID_TOKEN
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND_CHILD
import com.krince.reminisce.shared.response.SuccessResponse
import com.krince.reminisce.shared.response.SuccessResponseCode.CREATED
import com.krince.reminisce.shared.response.SuccessResponseCode.NO_CONTENT
import com.krince.reminisce.shared.response.SuccessResponseCode.OK
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody

@Tag(name = "아이(Children)")
interface ChildController {

    @Operation(summary = "아이 프로필 등록", description = "로그인한 보호자 계정 아래 아이 프로필을 등록합니다.")
    @SwaggerSuccessResponse(responseCode = CREATED, description = "아이 등록 성공")
    @SwaggerExceptionResponse(
        examples = [
            ExceptionExample(code = INVALID_DTO_PARAMETER, name = "요청 값 오류", message = "요청 값이 올바르지 않습니다.(dto 검증 오류)", description = "요청 바디 검증에 실패한 경우"),
            ExceptionExample(code = INVALID_CHILD_NICKNAME_LENGTH, name = "애칭 길이 오류", message = "아이 애칭 길이가 올바르지 않습니다.", description = "애칭 길이가 정책을 벗어난 경우"),
            ExceptionExample(code = EMPTY_TOKEN, name = "토큰 없음", message = "토큰이 없습니다.", description = "인증 토큰이 제공되지 않은 경우"),
            ExceptionExample(code = INVALID_TOKEN, name = "유효하지 않은 토큰", message = "유효하지 않은 토큰입니다.", description = "토큰이 유효하지 않거나 서명이 잘못된 경우"),
            ExceptionExample(code = EXPIRED_TOKEN, name = "만료된 토큰", message = "만료된 토큰입니다.", description = "토큰의 유효기간이 만료된 경우"),
            ExceptionExample(code = CHILD_LIMIT_EXCEEDED, name = "아이 수 초과", message = "등록 가능한 아이 수를 초과했습니다.", description = "보호자가 등록 가능한 아이 수 상한을 넘긴 경우"),
            ExceptionExample(code = INTERNAL_SERVER_ERROR, name = "서버 오류", message = "서버 에러입니다. 개발자에게 문의해주세요.", description = "예상치 못한 서버 오류가 발생한 경우"),
        ]
    )
    fun registerChild(
        @Valid @RequestBody request: RegisterChildRequest,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<SuccessResponse<ChildResponse>>

    @Operation(summary = "내 아이 목록 조회", description = "로그인한 보호자의 아이 목록을 조회합니다.")
    @SwaggerSuccessResponse(responseCode = OK, description = "아이 목록 조회 성공")
    @SwaggerExceptionResponse(
        examples = [
            ExceptionExample(code = EMPTY_TOKEN, name = "토큰 없음", message = "토큰이 없습니다.", description = "인증 토큰이 제공되지 않은 경우"),
            ExceptionExample(code = INVALID_TOKEN, name = "유효하지 않은 토큰", message = "유효하지 않은 토큰입니다.", description = "토큰이 유효하지 않거나 서명이 잘못된 경우"),
            ExceptionExample(code = EXPIRED_TOKEN, name = "만료된 토큰", message = "만료된 토큰입니다.", description = "토큰의 유효기간이 만료된 경우"),
            ExceptionExample(code = INTERNAL_SERVER_ERROR, name = "서버 오류", message = "서버 에러입니다. 개발자에게 문의해주세요.", description = "예상치 못한 서버 오류가 발생한 경우"),
        ]
    )
    fun getChildren(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<SuccessResponse<List<ChildResponse>>>

    @Operation(summary = "아이 단건 조회", description = "로그인한 보호자의 특정 아이 프로필을 조회합니다.")
    @SwaggerSuccessResponse(responseCode = OK, description = "아이 조회 성공")
    @SwaggerExceptionResponse(
        examples = [
            ExceptionExample(code = EMPTY_TOKEN, name = "토큰 없음", message = "토큰이 없습니다.", description = "인증 토큰이 제공되지 않은 경우"),
            ExceptionExample(code = INVALID_TOKEN, name = "유효하지 않은 토큰", message = "유효하지 않은 토큰입니다.", description = "토큰이 유효하지 않거나 서명이 잘못된 경우"),
            ExceptionExample(code = EXPIRED_TOKEN, name = "만료된 토큰", message = "만료된 토큰입니다.", description = "토큰의 유효기간이 만료된 경우"),
            ExceptionExample(code = NOT_FOUND_CHILD, name = "아이 없음", message = "아이가 존재하지 않습니다.", description = "조회하려는 아이가 없거나 다른 보호자의 아이인 경우"),
            ExceptionExample(code = INTERNAL_SERVER_ERROR, name = "서버 오류", message = "서버 에러입니다. 개발자에게 문의해주세요.", description = "예상치 못한 서버 오류가 발생한 경우"),
        ]
    )
    fun getChild(
        @Parameter(description = "아이 고유 식별자", example = "e443e5c3-0243-4d28-ba79-37cf3b923023", required = true)
        @NotBlank(message = "아이 식별자는 비어있을 수 없습니다.")
        @PathVariable
        childId: String,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<SuccessResponse<ChildResponse>>

    @Operation(
        summary = "아이 프로필 삭제",
        description = "로그인한 보호자의 아이 프로필과 그 아이의 모든 관련 데이터(말하기 세션·대화·발화분석·리포트·후활동·미션 결과·찜·단어장·동의 기록)를 영구 삭제합니다. 되돌릴 수 없습니다.",
    )
    @SwaggerSuccessResponse(responseCode = NO_CONTENT, description = "아이 삭제 성공")
    @SwaggerExceptionResponse(
        examples = [
            ExceptionExample(code = EMPTY_TOKEN, name = "토큰 없음", message = "토큰이 없습니다.", description = "인증 토큰이 제공되지 않은 경우"),
            ExceptionExample(code = INVALID_TOKEN, name = "유효하지 않은 토큰", message = "유효하지 않은 토큰입니다.", description = "토큰이 유효하지 않거나 서명이 잘못된 경우"),
            ExceptionExample(code = EXPIRED_TOKEN, name = "만료된 토큰", message = "만료된 토큰입니다.", description = "토큰의 유효기간이 만료된 경우"),
            ExceptionExample(code = NOT_FOUND_CHILD, name = "아이 없음", message = "아이가 존재하지 않습니다.", description = "삭제하려는 아이가 없거나 다른 보호자의 아이인 경우"),
            ExceptionExample(code = INTERNAL_SERVER_ERROR, name = "서버 오류", message = "서버 에러입니다. 개발자에게 문의해주세요.", description = "예상치 못한 서버 오류가 발생한 경우"),
        ]
    )
    fun deleteChild(
        @Parameter(description = "아이 고유 식별자", example = "e443e5c3-0243-4d28-ba79-37cf3b923023", required = true)
        @NotBlank(message = "아이 식별자는 비어있을 수 없습니다.")
        @PathVariable
        childId: String,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<Void>
}
