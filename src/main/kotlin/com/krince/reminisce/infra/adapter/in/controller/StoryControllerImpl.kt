package com.krince.reminisce.infra.adapter.`in`.controller

import com.krince.reminisce.application.port.`in`.story.command.GetRecommendedStoriesCommand
import com.krince.reminisce.application.port.`in`.story.command.GetStoriesCommand
import com.krince.reminisce.application.port.`in`.story.command.GetStoryCommand
import com.krince.reminisce.application.port.`in`.story.result.StoryDetailResult
import com.krince.reminisce.application.port.`in`.story.result.StorySummaryResult
import com.krince.reminisce.application.port.`in`.story.usecase.GetRecommendedStoriesUseCase
import com.krince.reminisce.application.port.`in`.story.usecase.GetStoriesUseCase
import com.krince.reminisce.application.port.`in`.story.usecase.GetStoryUseCase
import com.krince.reminisce.domain.model.story.vo.StoryGenre
import com.krince.reminisce.domain.model.story.vo.StorySort
import com.krince.reminisce.infra.adapter.`in`.dto.story.response.StoryDetailResponse
import com.krince.reminisce.infra.adapter.`in`.dto.story.response.StorySummaryResponse
import com.krince.reminisce.infra.adapter.`in`.dto.story.response.storyDetailResponse
import com.krince.reminisce.infra.adapter.`in`.dto.story.response.storySummaryResponse
import com.krince.reminisce.infra.security.CustomUserDetails
import com.krince.reminisce.shared.response.SuccessResponse
import com.krince.reminisce.shared.response.SuccessResponseCode.OK
import com.krince.reminisce.shared.response.successResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/api/stories")
class StoryControllerImpl(
    private val getStoriesUseCase: GetStoriesUseCase,
    private val getStoryUseCase: GetStoryUseCase,
    private val getRecommendedStoriesUseCase: GetRecommendedStoriesUseCase,
) : StoryController {

    @GetMapping
    override fun getStories(
        @RequestParam(required = false) topic: String?,
        @RequestParam(required = false) genre: StoryGenre?,
        @RequestParam(required = false) q: String?,
        @RequestParam(required = false) sort: StorySort?,
        @RequestParam(required = false) childId: String?,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<SuccessResponse<List<StorySummaryResponse>>> {
        val command = GetStoriesCommand(
            topic = topic,
            genre = genre,
            titleKeyword = q,
            sort = sort ?: StorySort.RECOMMENDED,
            childId = childId,
            guardianId = userDetails.getId(),
        )
        val results: List<StorySummaryResult> = getStoriesUseCase.execute(command)
        val response: List<StorySummaryResponse> = results.map { storySummaryResponse(result = it) }
        val responseBody: SuccessResponse<List<StorySummaryResponse>> =
            successResponse(responseCode = OK, data = response)

        return ResponseEntity.status(responseBody.code).body(responseBody)
    }

    @GetMapping("/{storyId}")
    override fun getStory(
        @PathVariable storyId: String,
    ): ResponseEntity<SuccessResponse<StoryDetailResponse>> {
        val command = GetStoryCommand(storyId = storyId)
        val result: StoryDetailResult = getStoryUseCase.execute(command)
        val response: StoryDetailResponse = storyDetailResponse(result = result)
        val responseBody: SuccessResponse<StoryDetailResponse> = successResponse(responseCode = OK, data = response)

        return ResponseEntity.status(responseBody.code).body(responseBody)
    }

    @GetMapping("/recommendations")
    override fun getRecommendedStories(
        @RequestParam childId: String,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<SuccessResponse<List<StorySummaryResponse>>> {
        val command = GetRecommendedStoriesCommand(childId = childId, guardianId = userDetails.getId())
        val results: List<StorySummaryResult> = getRecommendedStoriesUseCase.execute(command)
        val response: List<StorySummaryResponse> = results.map { storySummaryResponse(result = it) }
        val responseBody: SuccessResponse<List<StorySummaryResponse>> = successResponse(responseCode = OK, data = response)

        return ResponseEntity.status(responseBody.code).body(responseBody)
    }
}
