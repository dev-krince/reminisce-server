package com.krince.reminisce.infra.adapter.`in`.controller

import com.krince.reminisce.infra.adapter.`in`.dto.message.request.SubmitUtteranceRequest
import com.krince.reminisce.infra.adapter.`in`.dto.message.response.UtteranceResponse
import com.krince.reminisce.infra.adapter.`in`.dto.mission.request.SubmitMissionAnswerRequest
import com.krince.reminisce.infra.adapter.`in`.dto.mission.response.MissionAnswerResponse
import com.krince.reminisce.infra.adapter.`in`.dto.postactivity.request.SubmitCardOrderRequest
import com.krince.reminisce.infra.adapter.`in`.dto.postactivity.request.SubmitRetellingRequest
import com.krince.reminisce.infra.adapter.`in`.dto.postactivity.response.CardOrderResultResponse
import com.krince.reminisce.infra.adapter.`in`.dto.postactivity.response.RetellingResultResponse
import com.krince.reminisce.infra.adapter.`in`.dto.report.response.SessionReportResponse
import com.krince.reminisce.infra.adapter.`in`.dto.speakingsession.request.StartSpeakingSessionRequest
import com.krince.reminisce.infra.adapter.`in`.dto.speakingsession.response.SpeakingHintResponse
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
import com.krince.reminisce.shared.response.SuccessResponse
import com.krince.reminisce.shared.response.SuccessResponseCode.CREATED
import com.krince.reminisce.shared.response.SuccessResponseCode.NO_CONTENT
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
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.multipart.MultipartFile

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
        summary = "말하기 힌트 조회",
        description = "현재 대화 장면의 미션 예시를 힌트로 반환합니다. 아이가 '도움이 필요해요'를 누를 때 사용합니다. 미션이 없으면 빈 목록을 반환합니다.",
    )
    @SwaggerSuccessResponse(responseCode = OK, description = "힌트 조회 성공")
    @SwaggerExceptionResponse(
        examples = [
            ExceptionExample(code = EMPTY_TOKEN, name = "토큰 없음", message = "토큰이 없습니다.", description = "인증 토큰이 제공되지 않은 경우"),
            ExceptionExample(code = INVALID_TOKEN, name = "유효하지 않은 토큰", message = "유효하지 않은 토큰입니다.", description = "토큰이 유효하지 않거나 서명이 잘못된 경우"),
            ExceptionExample(code = EXPIRED_TOKEN, name = "만료된 토큰", message = "만료된 토큰입니다.", description = "토큰의 유효기간이 만료된 경우"),
            ExceptionExample(code = NOT_FOUND, name = "세션 없음", message = "리소스가 존재하지 않습니다.", description = "세션이 없거나 다른 보호자의 아이 세션인 경우"),
            ExceptionExample(code = INTERNAL_SERVER_ERROR, name = "서버 오류", message = "서버 에러입니다. 개발자에게 문의해주세요.", description = "예상치 못한 서버 오류가 발생한 경우"),
        ]
    )
    fun getSpeakingHint(
        @Parameter(description = "말하기 세션 고유 식별자", required = true) @PathVariable sessionId: String,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<SuccessResponse<SpeakingHintResponse>>

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
        description = "로그인한 보호자가 본인 아이의 진행 중 세션에서 현재 대화(DIALOGUE) 장면에 기기 STT로 확정한 발화 텍스트를 제출합니다. multipart/form-data로 request 파트(JSON: 발화 텍스트)와 선택 audio 파트(발화 녹음 음성 파일)를 받습니다. audio 파트가 없으면 음성 URL은 null입니다.",
    )
    @SwaggerSuccessResponse(responseCode = CREATED, description = "아이 발화 저장 성공")
    @SwaggerExceptionResponse(
        examples = [
            ExceptionExample(code = INVALID_DTO_PARAMETER, name = "요청 값 오류", message = "요청 값이 올바르지 않습니다.(dto 검증 오류)", description = "발화 텍스트가 비어있는 경우"),
            ExceptionExample(code = EMPTY_TOKEN, name = "토큰 없음", message = "토큰이 없습니다.", description = "인증 토큰이 제공되지 않은 경우"),
            ExceptionExample(code = INVALID_TOKEN, name = "유효하지 않은 토큰", message = "유효하지 않은 토큰입니다.", description = "토큰이 유효하지 않거나 서명이 잘못된 경우"),
            ExceptionExample(code = EXPIRED_TOKEN, name = "만료된 토큰", message = "만료된 토큰입니다.", description = "토큰의 유효기간이 만료된 경우"),
            ExceptionExample(code = NOT_FOUND, name = "세션 없음", message = "리소스가 존재하지 않습니다.", description = "세션이 없거나 다른 보호자의 아이 세션인 경우"),
            ExceptionExample(code = BUSINESS_RULE_VIOLATION, name = "발화 불가", message = "도메인 정책에 의해 실행할 수 없습니다.", description = "도입 상태이거나 대화 장면이 아닌 경우"),
            ExceptionExample(code = INTERNAL_SERVER_ERROR, name = "서버 오류", message = "서버 에러입니다. 개발자에게 문의해주세요.", description = "예상치 못한 서버 오류가 발생한 경우"),
        ]
    )
    fun submitUtterance(
        @Parameter(description = "말하기 세션 고유 식별자", required = true) @PathVariable sessionId: String,
        @Valid @RequestPart("request") request: SubmitUtteranceRequest,
        @RequestPart("audio", required = false) audio: MultipartFile?,
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
        summary = "미션 답안 제출",
        description = "로그인한 보호자가 본인 아이의 세션에서 미션이 있는 대화(DIALOGUE) 장면에 답안을 제출합니다. WORD_ORDER는 제출한 단어카드 순서가 정답 순서와 정확히 일치할 때 완료되고, SPEAKING은 발화 판정으로 완료 여부를 정합니다. 미완료면 힌트를 반환하고 무제한 재시도할 수 있으며 시도 횟수가 증가합니다.",
    )
    @SwaggerSuccessResponse(responseCode = OK, description = "미션 답안 제출 성공")
    @SwaggerExceptionResponse(
        examples = [
            ExceptionExample(code = EMPTY_TOKEN, name = "토큰 없음", message = "토큰이 없습니다.", description = "인증 토큰이 제공되지 않은 경우"),
            ExceptionExample(code = INVALID_TOKEN, name = "유효하지 않은 토큰", message = "유효하지 않은 토큰입니다.", description = "토큰이 유효하지 않거나 서명이 잘못된 경우"),
            ExceptionExample(code = EXPIRED_TOKEN, name = "만료된 토큰", message = "만료된 토큰입니다.", description = "토큰의 유효기간이 만료된 경우"),
            ExceptionExample(code = NOT_FOUND, name = "세션 없음", message = "리소스가 존재하지 않습니다.", description = "세션이 없거나 다른 보호자의 아이 세션인 경우"),
            ExceptionExample(code = BUSINESS_RULE_VIOLATION, name = "미션 제출 불가", message = "도메인 정책에 의해 실행할 수 없습니다.", description = "미션이 없는 장면이거나 대화(DIALOGUE) 장면이 아닌 경우"),
            ExceptionExample(code = INTERNAL_SERVER_ERROR, name = "서버 오류", message = "서버 에러입니다. 개발자에게 문의해주세요.", description = "예상치 못한 서버 오류가 발생한 경우"),
        ]
    )
    fun submitMissionAnswer(
        @Parameter(description = "말하기 세션 고유 식별자", required = true) @PathVariable sessionId: String,
        @Parameter(description = "미션 장면 고유 식별자", required = true) @PathVariable sceneId: String,
        @Valid @RequestBody request: SubmitMissionAnswerRequest,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<SuccessResponse<MissionAnswerResponse>>

    @Operation(
        summary = "이야기 재구성 발화 제출",
        description = "로그인한 보호자가 본인 아이의 POST_ACTIVITY 세션에서 카드 순서를 정답으로 맞춘 뒤 재구성 발화를 제출합니다. multipart/form-data로 request 파트(JSON: 기기 STT로 확정한 재구성 발화 텍스트)와 선택 audio 파트(재구성 녹음 음성 파일)를 받습니다. 재구성 텍스트와 음성 URL을 저장하고 세션을 완료 처리합니다. audio 파트가 없으면 음성 URL은 null입니다.",
    )
    @SwaggerSuccessResponse(responseCode = OK, description = "재구성 발화 제출 성공")
    @SwaggerExceptionResponse(
        examples = [
            ExceptionExample(code = INVALID_DTO_PARAMETER, name = "요청 값 오류", message = "요청 값이 올바르지 않습니다.(dto 검증 오류)", description = "재구성 발화 텍스트가 비어있는 경우"),
            ExceptionExample(code = EMPTY_TOKEN, name = "토큰 없음", message = "토큰이 없습니다.", description = "인증 토큰이 제공되지 않은 경우"),
            ExceptionExample(code = INVALID_TOKEN, name = "유효하지 않은 토큰", message = "유효하지 않은 토큰입니다.", description = "토큰이 유효하지 않거나 서명이 잘못된 경우"),
            ExceptionExample(code = EXPIRED_TOKEN, name = "만료된 토큰", message = "만료된 토큰입니다.", description = "토큰의 유효기간이 만료된 경우"),
            ExceptionExample(code = NOT_FOUND, name = "세션 없음", message = "리소스가 존재하지 않습니다.", description = "세션이 없거나 다른 보호자의 아이 세션인 경우"),
            ExceptionExample(code = BUSINESS_RULE_VIOLATION, name = "재구성 불가", message = "도메인 정책에 의해 실행할 수 없습니다.", description = "POST_ACTIVITY 상태가 아니거나 카드 순서를 아직 정답으로 맞추지 않은 경우"),
            ExceptionExample(code = INTERNAL_SERVER_ERROR, name = "서버 오류", message = "서버 에러입니다. 개발자에게 문의해주세요.", description = "예상치 못한 서버 오류가 발생한 경우"),
        ]
    )
    fun submitRetelling(
        @Parameter(description = "말하기 세션 고유 식별자", required = true) @PathVariable sessionId: String,
        @Valid @RequestPart("request") request: SubmitRetellingRequest,
        @RequestPart("audio", required = false) audio: MultipartFile?,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<SuccessResponse<RetellingResultResponse>>

    @Operation(
        summary = "말하기 세션 나가기(일시중지)",
        description = "로그인한 보호자가 본인 아이의 진행 중(IN_PROGRESS)이나 후활동(POST_ACTIVITY) 세션에서 나갑니다. 세션을 종료하지 않고 상태를 그대로 유지하며 마지막 활동 시각만 갱신하므로, 이어하기 목록에 남아 나중에 이어서 진행할 수 있습니다. 응답의 status는 STOPPED가 아니라 기존 상태(예: IN_PROGRESS)로 옵니다. 이미 완료(COMPLETED)·종료(STOPPED)된 세션은 거부합니다.",
    )
    @SwaggerSuccessResponse(responseCode = OK, description = "말하기 세션 나가기 성공")
    @SwaggerExceptionResponse(
        examples = [
            ExceptionExample(code = EMPTY_TOKEN, name = "토큰 없음", message = "토큰이 없습니다.", description = "인증 토큰이 제공되지 않은 경우"),
            ExceptionExample(code = INVALID_TOKEN, name = "유효하지 않은 토큰", message = "유효하지 않은 토큰입니다.", description = "토큰이 유효하지 않거나 서명이 잘못된 경우"),
            ExceptionExample(code = EXPIRED_TOKEN, name = "만료된 토큰", message = "만료된 토큰입니다.", description = "토큰의 유효기간이 만료된 경우"),
            ExceptionExample(code = NOT_FOUND, name = "세션 없음", message = "리소스가 존재하지 않습니다.", description = "세션이 없거나 다른 보호자의 아이 세션인 경우"),
            ExceptionExample(code = BUSINESS_RULE_VIOLATION, name = "나가기 불가", message = "도메인 정책에 의해 실행할 수 없습니다.", description = "이미 완료(COMPLETED)·종료(STOPPED)된 세션인 경우"),
            ExceptionExample(code = INTERNAL_SERVER_ERROR, name = "서버 오류", message = "서버 에러입니다. 개발자에게 문의해주세요.", description = "예상치 못한 서버 오류가 발생한 경우"),
        ]
    )
    fun stopSpeakingSession(
        @Parameter(description = "말하기 세션 고유 식별자", required = true) @PathVariable sessionId: String,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<SuccessResponse<SpeakingSessionResponse>>

    @Operation(
        summary = "말하기 세션 폐기",
        description = "로그인한 보호자가 본인 아이의 세션 하나를 상태와 무관하게 영구 삭제합니다. 그 세션의 대화·발화 분석·미션 결과·후활동 결과·리포트와 발화·재구성 음성 파일까지 함께 삭제되며 되돌릴 수 없습니다. 완료된 세션을 폐기하면 그 회차의 리포트도 사라집니다.",
    )
    @SwaggerSuccessResponse(responseCode = NO_CONTENT, description = "말하기 세션 폐기 성공")
    @SwaggerExceptionResponse(
        examples = [
            ExceptionExample(code = EMPTY_TOKEN, name = "토큰 없음", message = "토큰이 없습니다.", description = "인증 토큰이 제공되지 않은 경우"),
            ExceptionExample(code = INVALID_TOKEN, name = "유효하지 않은 토큰", message = "유효하지 않은 토큰입니다.", description = "토큰이 유효하지 않거나 서명이 잘못된 경우"),
            ExceptionExample(code = EXPIRED_TOKEN, name = "만료된 토큰", message = "만료된 토큰입니다.", description = "토큰의 유효기간이 만료된 경우"),
            ExceptionExample(code = NOT_FOUND, name = "세션 없음", message = "리소스가 존재하지 않습니다.", description = "세션이 없거나 다른 보호자의 아이 세션인 경우"),
            ExceptionExample(code = INTERNAL_SERVER_ERROR, name = "서버 오류", message = "서버 에러입니다. 개발자에게 문의해주세요.", description = "예상치 못한 서버 오류가 발생한 경우"),
        ]
    )
    fun deleteSpeakingSession(
        @Parameter(description = "말하기 세션 고유 식별자", required = true) @PathVariable sessionId: String,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<Void>

    @Operation(
        summary = "말하기 세션 이전 장면 되돌리기",
        description = "로그인한 보호자가 본인 아이의 진행 중(IN_PROGRESS) 세션을 바로 앞 장면으로 되돌립니다. 되돌린 장면의 진행 상태는 초기화되며 이후 진행은 유지하지 않습니다. 첫 장면이거나 도입 상태 세션은 거부합니다.",
    )
    @SwaggerSuccessResponse(responseCode = OK, description = "말하기 세션 이전 장면 되돌리기 성공")
    @SwaggerExceptionResponse(
        examples = [
            ExceptionExample(code = EMPTY_TOKEN, name = "토큰 없음", message = "토큰이 없습니다.", description = "인증 토큰이 제공되지 않은 경우"),
            ExceptionExample(code = INVALID_TOKEN, name = "유효하지 않은 토큰", message = "유효하지 않은 토큰입니다.", description = "토큰이 유효하지 않거나 서명이 잘못된 경우"),
            ExceptionExample(code = EXPIRED_TOKEN, name = "만료된 토큰", message = "만료된 토큰입니다.", description = "토큰의 유효기간이 만료된 경우"),
            ExceptionExample(code = NOT_FOUND, name = "세션 없음", message = "리소스가 존재하지 않습니다.", description = "세션이 없거나 다른 보호자의 아이 세션인 경우"),
            ExceptionExample(code = BUSINESS_RULE_VIOLATION, name = "되돌리기 불가", message = "도메인 정책에 의해 실행할 수 없습니다.", description = "진행 중이 아니거나 첫 장면·도입 상태 세션인 경우"),
            ExceptionExample(code = INTERNAL_SERVER_ERROR, name = "서버 오류", message = "서버 에러입니다. 개발자에게 문의해주세요.", description = "예상치 못한 서버 오류가 발생한 경우"),
        ]
    )
    fun goBackSpeakingScene(
        @Parameter(description = "말하기 세션 고유 식별자", required = true) @PathVariable sessionId: String,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<SuccessResponse<SpeakingSessionViewResponse>>

    @Operation(
        summary = "말하기 세션 보호자 리포트 조회",
        description = "로그인한 보호자가 본인 아이의 완료(COMPLETED) 세션 리포트를 5개 탭으로 조회합니다. " +
            "탭은 총합 요약(summaryTab: 아이 이름·이야기 제목·활동 날짜·소요 시간·후활동 완료 여부·총평·참여), " +
            "어휘·표현·논리(speechTab: 3영역), 장면별 발화(sceneTab: 대화 장면별 카드로 마지막 아이 발화·직전 캐릭터 질문·오디오), " +
            "대표 발화(representativeTab: 발화·오디오·해설), 가정 대화 가이드(homeGuideTab)입니다. " +
            "리포트가 없으면 세션 전체 발화 분석을 집계해 생성·저장한 뒤, 장면 카드와 총합 메타를 조회 시점에 조립해 반환합니다. " +
            "오디오를 제출하지 않은 발화의 음성 URL은 null입니다.",
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
