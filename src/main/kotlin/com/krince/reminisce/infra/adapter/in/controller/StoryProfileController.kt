package com.krince.reminisce.infra.adapter.`in`.controller

import com.krince.reminisce.infra.adapter.`in`.dto.storyprofile.response.StoryProfileResponse
import com.krince.reminisce.infra.security.CustomUserDetails
import com.krince.reminisce.infra.swagger.ExceptionExample
import com.krince.reminisce.infra.swagger.SwaggerExceptionResponse
import com.krince.reminisce.infra.swagger.SwaggerSuccessResponse
import com.krince.reminisce.shared.response.ExceptionResponseCode.EMPTY_TOKEN
import com.krince.reminisce.shared.response.ExceptionResponseCode.EXPIRED_TOKEN
import com.krince.reminisce.shared.response.ExceptionResponseCode.INTERNAL_SERVER_ERROR
import com.krince.reminisce.shared.response.ExceptionResponseCode.INVALID_TOKEN
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND_CHILD
import com.krince.reminisce.shared.response.SuccessResponse
import com.krince.reminisce.shared.response.SuccessResponseCode.OK
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PathVariable

@Tag(name = "이야기 프로필(Story Profiles)")
interface StoryProfileController {

    @Operation(
        summary = "아이 이야기 프로필 조회",
        description = "큐미 인터뷰 결과로 만든 아이의 이야기 프로필(관심 주제·잘하는 것·연습할 것·말하기 분석)을 조회합니다. 완료된 인터뷰가 있는데 프로필이 아직 없으면 이 요청에서 분석해 만들어 반환합니다.",
    )
    @SwaggerSuccessResponse(responseCode = OK, description = "이야기 프로필 조회 성공")
    @SwaggerExceptionResponse(
        examples = [
            ExceptionExample(code = EMPTY_TOKEN, name = "토큰 없음", message = "토큰이 없습니다.", description = "인증 토큰이 제공되지 않은 경우"),
            ExceptionExample(code = INVALID_TOKEN, name = "유효하지 않은 토큰", message = "유효하지 않은 토큰입니다.", description = "토큰이 유효하지 않거나 서명이 잘못된 경우"),
            ExceptionExample(code = EXPIRED_TOKEN, name = "만료된 토큰", message = "만료된 토큰입니다.", description = "토큰의 유효기간이 만료된 경우"),
            ExceptionExample(code = NOT_FOUND_CHILD, name = "아이 없음", message = "아이가 존재하지 않습니다.", description = "아이가 없거나 다른 보호자의 아이인 경우"),
            ExceptionExample(code = NOT_FOUND, name = "프로필 없음", message = "리소스가 존재하지 않습니다.", description = "완료된 큐미 인터뷰가 없어 프로필을 만들 수 없는 경우"),
            ExceptionExample(code = INTERNAL_SERVER_ERROR, name = "서버 오류", message = "서버 에러입니다. 개발자에게 문의해주세요.", description = "프로필 분석에 실패했거나 예상치 못한 서버 오류가 발생한 경우"),
        ]
    )
    fun getStoryProfile(
        @Parameter(description = "아이 고유 식별자", required = true)
        @PathVariable
        childId: String,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<SuccessResponse<StoryProfileResponse>>
}
