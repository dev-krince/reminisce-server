package com.krince.reminisce.infra.adapter.`in`.controller

import com.krince.reminisce.infra.adapter.`in`.dto.admin.request.UpdateInterviewStageTurnsRequest
import com.krince.reminisce.infra.adapter.`in`.dto.admin.request.UpdateSceneTurnsRequest
import com.krince.reminisce.infra.adapter.`in`.dto.admin.response.InterviewStageTurnsResponse
import com.krince.reminisce.infra.adapter.`in`.dto.admin.response.SceneTurnsResponse
import com.krince.reminisce.infra.swagger.ExceptionExample
import com.krince.reminisce.infra.swagger.SwaggerExceptionResponse
import com.krince.reminisce.infra.swagger.SwaggerSuccessResponse
import com.krince.reminisce.shared.response.ExceptionResponseCode.FORBIDDEN
import com.krince.reminisce.shared.response.ExceptionResponseCode.INTERNAL_SERVER_ERROR
import com.krince.reminisce.shared.response.ExceptionResponseCode.INVALID_DTO_PARAMETER
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND
import com.krince.reminisce.shared.response.SuccessResponse
import com.krince.reminisce.shared.response.SuccessResponseCode.OK
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody

@Tag(name = "운영 설정(Admin)", description = "인증 토큰 없이 호출합니다. 변경 요청은 body의 관리키(adminKey)로 보호됩니다.")
interface AdminSettingsController {

    @Operation(
        summary = "큐미 인터뷰 단계별 답 횟수 조회",
        description = "큐미 인터뷰의 단계별 아이 답 횟수와 전체 합계를 조회합니다. 인증·관리키 없이 호출할 수 있습니다.",
    )
    @SwaggerSuccessResponse(responseCode = OK, description = "설정 조회 성공")
    @SwaggerExceptionResponse(
        examples = [
            ExceptionExample(code = INTERNAL_SERVER_ERROR, name = "서버 오류", message = "서버 에러입니다. 개발자에게 문의해주세요.", description = "예상치 못한 서버 오류가 발생한 경우"),
        ]
    )
    fun getInterviewStageTurns(): ResponseEntity<SuccessResponse<InterviewStageTurnsResponse>>

    @Operation(
        summary = "큐미 인터뷰 단계별 답 횟수 변경",
        description = "큐미 인터뷰의 단계별 아이 답 횟수를 변경합니다(각 0~10, 0이면 그 단계 건너뜀, 전부 0은 거부). 변경 즉시 적용되며 진행 중 인터뷰도 다음 답부터 새 설정을 따릅니다. body의 관리키(adminKey=reminisce)가 일치해야 합니다.",
    )
    @SwaggerSuccessResponse(responseCode = OK, description = "설정 변경 성공")
    @SwaggerExceptionResponse(
        examples = [
            ExceptionExample(code = INVALID_DTO_PARAMETER, name = "요청 값 오류", message = "요청 값이 올바르지 않습니다.(dto 검증 오류)", description = "0~10 범위를 벗어났거나 전부 0인 경우"),
            ExceptionExample(code = FORBIDDEN, name = "관리키 불일치", message = "해당 리소스에 접근 권한이 없습니다.", description = "adminKey가 일치하지 않는 경우"),
            ExceptionExample(code = INTERNAL_SERVER_ERROR, name = "서버 오류", message = "서버 에러입니다. 개발자에게 문의해주세요.", description = "예상치 못한 서버 오류가 발생한 경우"),
        ]
    )
    fun updateInterviewStageTurns(
        @Valid @RequestBody request: UpdateInterviewStageTurnsRequest,
    ): ResponseEntity<SuccessResponse<InterviewStageTurnsResponse>>

    @Operation(
        summary = "이야기 장면 발화 횟수 변경",
        description = "대화 장면 하나의 최소 발화(preferredTurns)·최대 발화(maxTurns)를 변경합니다. 보낸 값만 바뀌고 생략한 값은 유지됩니다. body의 관리키(adminKey=reminisce)가 일치해야 합니다. 현재 값 확인은 이야기 상세 조회(GET /api/stories/{storyId})의 장면 필드로 볼 수 있습니다.",
    )
    @SwaggerSuccessResponse(responseCode = OK, description = "장면 발화 횟수 변경 성공")
    @SwaggerExceptionResponse(
        examples = [
            ExceptionExample(code = INVALID_DTO_PARAMETER, name = "요청 값 오류", message = "요청 값이 올바르지 않습니다.(dto 검증 오류)", description = "1~10 범위를 벗어났거나 최소가 최대보다 큰 경우"),
            ExceptionExample(code = FORBIDDEN, name = "관리키 불일치", message = "해당 리소스에 접근 권한이 없습니다.", description = "adminKey가 일치하지 않는 경우"),
            ExceptionExample(code = NOT_FOUND, name = "장면 없음", message = "리소스가 존재하지 않습니다.", description = "장면이 존재하지 않는 경우"),
            ExceptionExample(code = INTERNAL_SERVER_ERROR, name = "서버 오류", message = "서버 에러입니다. 개발자에게 문의해주세요.", description = "예상치 못한 서버 오류가 발생한 경우"),
        ]
    )
    fun updateSceneTurns(
        @Parameter(description = "장면 고유 식별자", example = "sc_banggui_04", required = true)
        @PathVariable
        sceneId: String,
        @Valid @RequestBody request: UpdateSceneTurnsRequest,
    ): ResponseEntity<SuccessResponse<SceneTurnsResponse>>
}
