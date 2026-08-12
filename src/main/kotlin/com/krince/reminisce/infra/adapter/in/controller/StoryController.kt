package com.krince.reminisce.infra.adapter.`in`.controller

import com.krince.reminisce.domain.model.story.vo.StoryGenre
import com.krince.reminisce.domain.model.story.vo.StorySort
import com.krince.reminisce.infra.adapter.`in`.dto.story.response.StoryDetailResponse
import com.krince.reminisce.infra.adapter.`in`.dto.story.response.StorySummaryResponse
import com.krince.reminisce.infra.swagger.ExceptionExample
import com.krince.reminisce.infra.swagger.SwaggerExceptionResponse
import com.krince.reminisce.infra.swagger.SwaggerSuccessResponse
import com.krince.reminisce.infra.security.CustomUserDetails
import com.krince.reminisce.shared.response.ExceptionResponseCode.EMPTY_TOKEN
import com.krince.reminisce.shared.response.ExceptionResponseCode.EXPIRED_TOKEN
import com.krince.reminisce.shared.response.ExceptionResponseCode.INTERNAL_SERVER_ERROR
import com.krince.reminisce.shared.response.ExceptionResponseCode.INVALID_TOKEN
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND_STORY
import com.krince.reminisce.shared.response.SuccessResponse
import com.krince.reminisce.shared.response.SuccessResponseCode.OK
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.NotBlank
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam

@Tag(name = "이야기(Stories)")
interface StoryController {

    @Operation(
        summary = "이야기 목록 조회",
        description = "공개된 이야기 목록을 조회합니다. topic·genre로 필터링하고 q로 제목 부분검색, sort로 정렬합니다.",
    )
    @SwaggerSuccessResponse(responseCode = OK, description = "이야기 목록 조회 성공")
    @SwaggerExceptionResponse(
        examples = [
            ExceptionExample(code = EMPTY_TOKEN, name = "토큰 없음", message = "토큰이 없습니다.", description = "인증 토큰이 제공되지 않은 경우"),
            ExceptionExample(code = INVALID_TOKEN, name = "유효하지 않은 토큰", message = "유효하지 않은 토큰입니다.", description = "토큰이 유효하지 않거나 서명이 잘못된 경우"),
            ExceptionExample(code = EXPIRED_TOKEN, name = "만료된 토큰", message = "만료된 토큰입니다.", description = "토큰의 유효기간이 만료된 경우"),
            ExceptionExample(code = INTERNAL_SERVER_ERROR, name = "서버 오류", message = "서버 에러입니다. 개발자에게 문의해주세요.", description = "예상치 못한 서버 오류가 발생한 경우"),
        ]
    )
    fun getStories(
        @Parameter(description = "필터링할 주제", example = "다름", required = false)
        @RequestParam(required = false)
        topic: String?,
        @Parameter(description = "필터링할 장르", example = "FOLKTALE", required = false)
        @RequestParam(required = false)
        genre: StoryGenre?,
        @Parameter(description = "제목 부분검색어", example = "며느리", required = false)
        @RequestParam(required = false)
        q: String?,
        @Parameter(description = "정렬 기준 (미지정 시 RECOMMENDED)", example = "RECOMMENDED", required = false)
        @RequestParam(required = false)
        sort: StorySort?,
    ): ResponseEntity<SuccessResponse<List<StorySummaryResponse>>>

    @Operation(summary = "이야기 상세 조회", description = "공개된 이야기의 상세 정보와 순서대로 정렬된 장면 목록을 조회합니다.")
    @SwaggerSuccessResponse(responseCode = OK, description = "이야기 상세 조회 성공")
    @SwaggerExceptionResponse(
        examples = [
            ExceptionExample(code = EMPTY_TOKEN, name = "토큰 없음", message = "토큰이 없습니다.", description = "인증 토큰이 제공되지 않은 경우"),
            ExceptionExample(code = INVALID_TOKEN, name = "유효하지 않은 토큰", message = "유효하지 않은 토큰입니다.", description = "토큰이 유효하지 않거나 서명이 잘못된 경우"),
            ExceptionExample(code = EXPIRED_TOKEN, name = "만료된 토큰", message = "만료된 토큰입니다.", description = "토큰의 유효기간이 만료된 경우"),
            ExceptionExample(code = NOT_FOUND_STORY, name = "이야기 없음", message = "이야기가 존재하지 않습니다.", description = "이야기가 없거나 공개 상태가 아닌 경우"),
            ExceptionExample(code = INTERNAL_SERVER_ERROR, name = "서버 오류", message = "서버 에러입니다. 개발자에게 문의해주세요.", description = "예상치 못한 서버 오류가 발생한 경우"),
        ]
    )
    fun getStory(
        @Parameter(description = "이야기 고유 식별자", example = "s_banggui_daughter_in_law_001", required = true)
        @NotBlank(message = "이야기 식별자는 비어있을 수 없습니다.")
        @PathVariable
        storyId: String,
    ): ResponseEntity<SuccessResponse<StoryDetailResponse>>

    @Operation(summary = "추천 이야기 목록 조회", description = "인증 보호자의 아이가 아직 시작하지 않은 게시 이야기를 난이도 오름차순으로 최대 10개 반환합니다.")
    @SwaggerSuccessResponse(responseCode = OK, description = "추천 이야기 목록 조회 성공")
    @SwaggerExceptionResponse(
        examples = [
            ExceptionExample(code = EMPTY_TOKEN, name = "토큰 없음", message = "토큰이 없습니다.", description = "인증 토큰이 제공되지 않은 경우"),
            ExceptionExample(code = INVALID_TOKEN, name = "유효하지 않은 토큰", message = "유효하지 않은 토큰입니다.", description = "토큰이 유효하지 않거나 서명이 잘못된 경우"),
            ExceptionExample(code = EXPIRED_TOKEN, name = "만료된 토큰", message = "만료된 토큰입니다.", description = "토큰의 유효기간이 만료된 경우"),
            ExceptionExample(code = NOT_FOUND, name = "아이 없음", message = "리소스가 존재하지 않습니다.", description = "존재하지 않거나 다른 보호자의 아이인 경우"),
            ExceptionExample(code = INTERNAL_SERVER_ERROR, name = "서버 오류", message = "서버 에러입니다. 개발자에게 문의해주세요.", description = "예상치 못한 서버 오류가 발생한 경우"),
        ]
    )
    fun getRecommendedStories(
        @Parameter(description = "아이 고유 식별자", required = true)
        @RequestParam
        childId: String,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<SuccessResponse<List<StorySummaryResponse>>>
}
