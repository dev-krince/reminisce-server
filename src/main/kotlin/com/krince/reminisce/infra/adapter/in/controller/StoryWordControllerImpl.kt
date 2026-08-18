package com.krince.reminisce.infra.adapter.`in`.controller

import com.krince.reminisce.application.port.`in`.wordbook.command.GetStoryWordsCommand
import com.krince.reminisce.application.port.`in`.wordbook.result.StoryWordGroupResult
import com.krince.reminisce.application.port.`in`.wordbook.usecase.GetStoryWordsUseCase
import com.krince.reminisce.infra.adapter.`in`.dto.wordbook.response.StoryWordGroupResponse
import com.krince.reminisce.infra.adapter.`in`.dto.wordbook.response.storyWordGroupResponse
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
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/api/children/{childId}/story-words")
class StoryWordControllerImpl(
    private val getStoryWordsUseCase: GetStoryWordsUseCase,
) : StoryWordController {

    @GetMapping
    override fun getStoryWords(
        @PathVariable childId: String,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<SuccessResponse<List<StoryWordGroupResponse>>> {
        val command = GetStoryWordsCommand(childId = childId, guardianId = userDetails.getId())
        val results: List<StoryWordGroupResult> = getStoryWordsUseCase.execute(command)
        val response: List<StoryWordGroupResponse> = results.map { storyWordGroupResponse(it) }
        val responseBody: SuccessResponse<List<StoryWordGroupResponse>> = successResponse(responseCode = OK, data = response)

        return ResponseEntity.status(responseBody.code).body(responseBody)
    }
}
