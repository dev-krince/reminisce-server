package com.krince.reminisce.infra.adapter.`in`.controller

import com.krince.reminisce.infra.adapter.`in`.dto.message.request.SubmitUtteranceRequest
import com.krince.reminisce.infra.adapter.`in`.dto.message.response.UtteranceResponse
import com.krince.reminisce.infra.adapter.`in`.dto.postactivity.request.SubmitCardOrderRequest
import com.krince.reminisce.infra.adapter.`in`.dto.postactivity.request.SubmitRetellingRequest
import com.krince.reminisce.infra.adapter.`in`.dto.postactivity.response.CardOrderResultResponse
import com.krince.reminisce.infra.adapter.`in`.dto.postactivity.response.RetellingResultResponse
import com.krince.reminisce.infra.adapter.`in`.dto.report.response.SessionReportResponse
import com.krince.reminisce.infra.adapter.`in`.dto.speakingsession.request.StartSpeakingSessionRequest
import com.krince.reminisce.infra.adapter.`in`.dto.speakingsession.response.SpeakingSessionResponse
import com.krince.reminisce.infra.adapter.`in`.dto.speakingsession.response.SpeakingSessionSummaryResponse
import com.krince.reminisce.infra.adapter.`in`.dto.speakingsession.response.SpeakingSessionViewResponse
import com.krince.reminisce.infra.security.CustomUserDetails
import com.krince.reminisce.infra.swagger.ExceptionExample
import com.krince.reminisce.infra.swagger.SwaggerExceptionResponse
import com.krince.reminisce.infra.swagger.SwaggerSuccessResponse
import com.krince.reminisce.shared.response.ExceptionResponseCode.BUSINESS_RULE_VIOLATION
import com.krince.reminisce.shared.response.ExceptionResponseCode.CONSENT_REQUIRED
import com.krince.reminisce.shared.response.ExceptionResponseCode.EMPTY_TOKEN
import com.krince.reminisce.shared.response.ExceptionResponseCode.EXPIRED_TOKEN
import com.krince.reminisce.shared.response.ExceptionResponseCode.INTERNAL_SERVER_ERROR
import com.krince.reminisce.shared.response.ExceptionResponseCode.INVALID_DTO_PARAMETER
import com.krince.reminisce.shared.response.ExceptionResponseCode.INVALID_TOKEN
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND_CHILD
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND_STORY
import com.krince.reminisce.shared.response.ExceptionResponseCode.STT_TRANSCRIPTION_FAILED
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
import org.springframework.web.bind.annotation.RequestParam

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

    @Operation(
        summary = "말하기 세션 현재 뷰 조회",
        description = "로그인한 보호자가 본인 아이의 세션 현재 뷰를 조회합니다. current_scene_id가 없으면 도입(INTRO), 있으면 현재 장면(SCENE)을 반환합니다.",
    )
    @SwaggerSuccessResponse(responseCode = OK, description = "말하기 세션 현재 뷰 조회 성공")
    @SwaggerExceptionResponse(
        examples = [
            ExceptionExample(code = EMPTY_TOKEN, name = "토큰 없음", message = "토큰이 없습니다.", description = "인증 토큰이 제공되지 않은 경우"),
            ExceptionExample(code = INVALID_TOKEN, name = "유효하지 않은 토큰", message = "유효하지 않은 토큰입니다.", description = "토큰이 유효하지 않거나 서명이 잘못된 경우"),
            ExceptionExample(code = EXPIRED_TOKEN, name = "만료된 토큰", message = "만료된 토큰입니다.", description = "토큰의 유효기간이 만료된 경우"),
            ExceptionExample(code = NOT_FOUND, name = "세션 없음", message = "리소스가 존재하지 않습니다.", description = "세션이 없거나 다른 보호자의 아이 세션인 경우"),
            ExceptionExample(code = INTERNAL_SERVER_ERROR, name = "서버 오류", message = "서버 에러입니다. 개발자에게 문의해주세요.", description = "예상치 못한 서버 오류가 발생한 경우"),
        ]
    )
    fun getSpeakingSessionView(
        @Parameter(description = "말하기 세션 고유 식별자", required = true) @PathVariable sessionId: String,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<SuccessResponse<SpeakingSessionViewResponse>>

    @Operation(
        summary = "말하기 세션 첫 장면 진입",
        description = "로그인한 보호자가 본인 아이의 도입 상태 세션을 첫 장면으로 진입시킵니다. 이미 장면 위 세션이면 거부합니다.",
    )
    @SwaggerSuccessResponse(responseCode = OK, description = "말하기 세션 첫 장면 진입 성공")
    @SwaggerExceptionResponse(
        examples = [
            ExceptionExample(code = EMPTY_TOKEN, name = "토큰 없음", message = "토큰이 없습니다.", description = "인증 토큰이 제공되지 않은 경우"),
            ExceptionExample(code = INVALID_TOKEN, name = "유효하지 않은 토큰", message = "유효하지 않은 토큰입니다.", description = "토큰이 유효하지 않거나 서명이 잘못된 경우"),
            ExceptionExample(code = EXPIRED_TOKEN, name = "만료된 토큰", message = "만료된 토큰입니다.", description = "토큰의 유효기간이 만료된 경우"),
            ExceptionExample(code = NOT_FOUND, name = "세션 없음", message = "리소스가 존재하지 않습니다.", description = "세션이 없거나 다른 보호자의 아이 세션인 경우"),
            ExceptionExample(code = BUSINESS_RULE_VIOLATION, name = "장면 진행 불가", message = "도메인 정책에 의해 실행할 수 없습니다.", description = "이미 장면 위에 있는 세션인 경우"),
            ExceptionExample(code = INTERNAL_SERVER_ERROR, name = "서버 오류", message = "서버 에러입니다. 개발자에게 문의해주세요.", description = "예상치 못한 서버 오류가 발생한 경우"),
        ]
    )
    fun advanceSpeakingScene(
        @Parameter(description = "말하기 세션 고유 식별자", required = true) @PathVariable sessionId: String,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<SuccessResponse<SpeakingSessionViewResponse>>

    @Operation(
        summary = "아이 발화 제출",
        description = "로그인한 보호자가 본인 아이의 진행 중 세션에서 현재 대화(DIALOGUE) 장면에 발화를 제출합니다. STT 성공 시 아이 메시지 1건을 저장합니다.",
    )
    @SwaggerSuccessResponse(responseCode = CREATED, description = "아이 발화 저장 성공")
    @SwaggerExceptionResponse(
        examples = [
            ExceptionExample(code = EMPTY_TOKEN, name = "토큰 없음", message = "토큰이 없습니다.", description = "인증 토큰이 제공되지 않은 경우"),
            ExceptionExample(code = INVALID_TOKEN, name = "유효하지 않은 토큰", message = "유효하지 않은 토큰입니다.", description = "토큰이 유효하지 않거나 서명이 잘못된 경우"),
            ExceptionExample(code = EXPIRED_TOKEN, name = "만료된 토큰", message = "만료된 토큰입니다.", description = "토큰의 유효기간이 만료된 경우"),
            ExceptionExample(code = NOT_FOUND, name = "세션 없음", message = "리소스가 존재하지 않습니다.", description = "세션이 없거나 다른 보호자의 아이 세션인 경우"),
            ExceptionExample(code = BUSINESS_RULE_VIOLATION, name = "발화 불가", message = "도메인 정책에 의해 실행할 수 없습니다.", description = "도입 상태이거나 대화 장면이 아닌 경우"),
            ExceptionExample(code = STT_TRANSCRIPTION_FAILED, name = "STT 실패", message = "음성 인식에 실패해 발화를 저장할 수 없습니다.", description = "음성 인식에 실패해 확정 텍스트가 없는 경우"),
            ExceptionExample(code = INTERNAL_SERVER_ERROR, name = "서버 오류", message = "서버 에러입니다. 개발자에게 문의해주세요.", description = "예상치 못한 서버 오류가 발생한 경우"),
        ]
    )
    fun submitUtterance(
        @Parameter(description = "말하기 세션 고유 식별자", required = true) @PathVariable sessionId: String,
        @Valid @RequestBody request: SubmitUtteranceRequest,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<SuccessResponse<UtteranceResponse>>

    @Operation(
        summary = "이어하기 세션 목록 조회",
        description = "로그인한 보호자가 본인 아이의 진행 중(IN_PROGRESS) 세션 목록을 최근 활동 순으로 조회합니다.",
    )
    @SwaggerSuccessResponse(responseCode = OK, description = "이어하기 세션 목록 조회 성공")
    @SwaggerExceptionResponse(
        examples = [
            ExceptionExample(code = EMPTY_TOKEN, name = "토큰 없음", message = "토큰이 없습니다.", description = "인증 토큰이 제공되지 않은 경우"),
            ExceptionExample(code = INVALID_TOKEN, name = "유효하지 않은 토큰", message = "유효하지 않은 토큰입니다.", description = "토큰이 유효하지 않거나 서명이 잘못된 경우"),
            ExceptionExample(code = EXPIRED_TOKEN, name = "만료된 토큰", message = "만료된 토큰입니다.", description = "토큰의 유효기간이 만료된 경우"),
            ExceptionExample(code = NOT_FOUND, name = "아이 없음 또는 타 보호자 아이", message = "리소스가 존재하지 않습니다.", description = "아이가 없거나 다른 보호자의 아이인 경우"),
            ExceptionExample(code = INTERNAL_SERVER_ERROR, name = "서버 오류", message = "서버 에러입니다. 개발자에게 문의해주세요.", description = "예상치 못한 서버 오류가 발생한 경우"),
        ]
    )
    fun getResumableSessions(
        @Parameter(description = "아이 고유 식별자", required = true) @RequestParam childId: String,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<SuccessResponse<List<SpeakingSessionSummaryResponse>>>

    @Operation(
        summary = "카드 순서 제출",
        description = "로그인한 보호자가 본인 아이의 POST_ACTIVITY 세션에 카드 배열 순서를 제출합니다. 서버가 정답 여부를 계산합니다.",
    )
    @SwaggerSuccessResponse(responseCode = OK, description = "카드 순서 제출 성공")
    @SwaggerExceptionResponse(
        examples = [
            ExceptionExample(code = INVALID_DTO_PARAMETER, name = "요청 값 오류", message = "요청 값이 올바르지 않습니다.(dto 검증 오류)", description = "요청 바디 검증에 실패한 경우"),
            ExceptionExample(code = EMPTY_TOKEN, name = "토큰 없음", message = "토큰이 없습니다.", description = "인증 토큰이 제공되지 않은 경우"),
            ExceptionExample(code = INVALID_TOKEN, name = "유효하지 않은 토큰", message = "유효하지 않은 토큰입니다.", description = "토큰이 유효하지 않거나 서명이 잘못된 경우"),
            ExceptionExample(code = EXPIRED_TOKEN, name = "만료된 토큰", message = "만료된 토큰입니다.", description = "토큰의 유효기간이 만료된 경우"),
            ExceptionExample(code = NOT_FOUND, name = "세션 없음", message = "리소스가 존재하지 않습니다.", description = "세션이 없거나 다른 보호자의 아이 세션인 경우"),
            ExceptionExample(code = BUSINESS_RULE_VIOLATION, name = "후활동 불가", message = "도메인 정책에 의해 실행할 수 없습니다.", description = "POST_ACTIVITY 상태가 아닌 세션인 경우"),
            ExceptionExample(code = INTERNAL_SERVER_ERROR, name = "서버 오류", message = "서버 에러입니다. 개발자에게 문의해주세요.", description = "예상치 못한 서버 오류가 발생한 경우"),
        ]
    )
    fun submitCardOrder(
        @Parameter(description = "말하기 세션 고유 식별자", required = true) @PathVariable sessionId: String,
        @Valid @RequestBody request: SubmitCardOrderRequest,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<SuccessResponse<CardOrderResultResponse>>

    @Operation(
        summary = "이야기 재구성 발화 제출",
        description = "로그인한 보호자가 본인 아이의 POST_ACTIVITY 세션에서 카드 순서를 정답으로 맞춘 뒤 아이의 재구성 발화를 제출합니다. STT 성공 시 재구성 텍스트를 저장하고 세션을 완료 처리합니다.",
    )
    @SwaggerSuccessResponse(responseCode = OK, description = "재구성 발화 제출 성공")
    @SwaggerExceptionResponse(
        examples = [
            ExceptionExample(code = INVALID_DTO_PARAMETER, name = "요청 값 오류", message = "요청 값이 올바르지 않습니다.(dto 검증 오류)", description = "요청 바디 검증에 실패한 경우"),
            ExceptionExample(code = EMPTY_TOKEN, name = "토큰 없음", message = "토큰이 없습니다.", description = "인증 토큰이 제공되지 않은 경우"),
            ExceptionExample(code = INVALID_TOKEN, name = "유효하지 않은 토큰", message = "유효하지 않은 토큰입니다.", description = "토큰이 유효하지 않거나 서명이 잘못된 경우"),
            ExceptionExample(code = EXPIRED_TOKEN, name = "만료된 토큰", message = "만료된 토큰입니다.", description = "토큰의 유효기간이 만료된 경우"),
            ExceptionExample(code = NOT_FOUND, name = "세션 없음", message = "리소스가 존재하지 않습니다.", description = "세션이 없거나 다른 보호자의 아이 세션인 경우"),
            ExceptionExample(code = BUSINESS_RULE_VIOLATION, name = "재구성 불가", message = "도메인 정책에 의해 실행할 수 없습니다.", description = "POST_ACTIVITY 상태가 아니거나 카드 순서를 아직 정답으로 맞추지 않은 경우"),
            ExceptionExample(code = STT_TRANSCRIPTION_FAILED, name = "STT 실패", message = "음성 인식에 실패해 발화를 저장할 수 없습니다.", description = "음성 인식에 실패해 확정 텍스트가 없는 경우"),
            ExceptionExample(code = INTERNAL_SERVER_ERROR, name = "서버 오류", message = "서버 에러입니다. 개발자에게 문의해주세요.", description = "예상치 못한 서버 오류가 발생한 경우"),
        ]
    )
    fun submitRetelling(
        @Parameter(description = "말하기 세션 고유 식별자", required = true) @PathVariable sessionId: String,
        @Valid @RequestBody request: SubmitRetellingRequest,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<SuccessResponse<RetellingResultResponse>>

    @Operation(
        summary = "말하기 세션 보호자 리포트 조회",
        description = "로그인한 보호자가 본인 아이의 완료(COMPLETED) 세션 리포트를 조회합니다. 리포트가 없으면 세션 전체 발화 분석을 집계해 강점·다음 초점·요약을 생성·저장한 뒤 반환합니다.",
    )
    @SwaggerSuccessResponse(responseCode = OK, description = "말하기 세션 보호자 리포트 조회 성공")
    @SwaggerExceptionResponse(
        examples = [
            ExceptionExample(code = EMPTY_TOKEN, name = "토큰 없음", message = "토큰이 없습니다.", description = "인증 토큰이 제공되지 않은 경우"),
            ExceptionExample(code = INVALID_TOKEN, name = "유효하지 않은 토큰", message = "유효하지 않은 토큰입니다.", description = "토큰이 유효하지 않거나 서명이 잘못된 경우"),
            ExceptionExample(code = EXPIRED_TOKEN, name = "만료된 토큰", message = "만료된 토큰입니다.", description = "토큰의 유효기간이 만료된 경우"),
            ExceptionExample(code = NOT_FOUND, name = "세션 없음", message = "리소스가 존재하지 않습니다.", description = "세션이 없거나 다른 보호자의 아이 세션인 경우"),
            ExceptionExample(code = BUSINESS_RULE_VIOLATION, name = "리포트 불가", message = "도메인 정책에 의해 실행할 수 없습니다.", description = "완료(COMPLETED) 상태가 아닌 세션인 경우"),
            ExceptionExample(code = INTERNAL_SERVER_ERROR, name = "서버 오류", message = "서버 에러입니다. 개발자에게 문의해주세요.", description = "예상치 못한 서버 오류가 발생한 경우"),
        ]
    )
    fun getSessionReport(
        @Parameter(description = "말하기 세션 고유 식별자", required = true) @PathVariable sessionId: String,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<SuccessResponse<SessionReportResponse>>
}
