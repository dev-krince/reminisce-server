package com.krince.reminisce.infra.adapter.`in`.controller

import com.krince.reminisce.application.port.`in`.storyprofile.command.GetStoryProfileCommand
import com.krince.reminisce.application.port.`in`.storyprofile.result.StoryProfileResult
import com.krince.reminisce.application.port.`in`.storyprofile.usecase.GetStoryProfileUseCase
import com.krince.reminisce.infra.adapter.`in`.dto.storyprofile.response.StoryProfileResponse
import com.krince.reminisce.infra.adapter.`in`.dto.storyprofile.response.storyProfileResponse
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
@RequestMapping("/api/children/{childId}/story-profile")
class StoryProfileControllerImpl(
    private val getStoryProfileUseCase: GetStoryProfileUseCase,
) : StoryProfileController {

    @GetMapping
    override fun getStoryProfile(
        @PathVariable childId: String,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<SuccessResponse<StoryProfileResponse>> {
        val command = GetStoryProfileCommand(guardianId = userDetails.getId(), childId = childId)
        val result: StoryProfileResult = getStoryProfileUseCase.execute(command)
        val response: StoryProfileResponse = storyProfileResponse(result = result)
        val responseBody: SuccessResponse<StoryProfileResponse> = successResponse(responseCode = OK, data = response)

        return ResponseEntity.status(responseBody.code).body(responseBody)
    }
}
