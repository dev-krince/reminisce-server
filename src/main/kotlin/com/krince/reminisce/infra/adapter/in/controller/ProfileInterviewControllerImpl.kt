package com.krince.reminisce.infra.adapter.`in`.controller

import com.krince.reminisce.application.port.`in`.profileinterview.command.StartProfileInterviewCommand
import com.krince.reminisce.application.port.`in`.profileinterview.command.SubmitInterviewUtteranceCommand
import com.krince.reminisce.application.port.`in`.profileinterview.result.ProfileInterviewResult
import com.krince.reminisce.application.port.`in`.profileinterview.usecase.StartProfileInterviewUseCase
import com.krince.reminisce.application.port.`in`.profileinterview.usecase.SubmitInterviewUtteranceUseCase
import com.krince.reminisce.infra.adapter.`in`.dto.profileinterview.request.StartProfileInterviewRequest
import com.krince.reminisce.infra.adapter.`in`.dto.profileinterview.request.SubmitInterviewUtteranceRequest
import com.krince.reminisce.infra.adapter.`in`.dto.profileinterview.response.ProfileInterviewResponse
import com.krince.reminisce.infra.adapter.`in`.dto.profileinterview.response.profileInterviewResponse
import com.krince.reminisce.infra.security.CustomUserDetails
import com.krince.reminisce.shared.response.SuccessResponse
import com.krince.reminisce.shared.response.SuccessResponseCode
import com.krince.reminisce.shared.response.SuccessResponseCode.CREATED
import com.krince.reminisce.shared.response.SuccessResponseCode.OK
import com.krince.reminisce.shared.response.successResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/api/profile-interviews")
class ProfileInterviewControllerImpl(
    private val startProfileInterviewUseCase: StartProfileInterviewUseCase,
    private val submitInterviewUtteranceUseCase: SubmitInterviewUtteranceUseCase,
) : ProfileInterviewController {

    @PostMapping
    override fun startProfileInterview(
        @Valid @RequestBody request: StartProfileInterviewRequest,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<SuccessResponse<ProfileInterviewResponse>> {
        val command = StartProfileInterviewCommand(guardianId = userDetails.getId(), childId = request.childId)
        val result: ProfileInterviewResult = startProfileInterviewUseCase.execute(command)
        val response: ProfileInterviewResponse = profileInterviewResponse(result = result)
        val responseCode: SuccessResponseCode = if (result.created) CREATED else OK
        val responseBody: SuccessResponse<ProfileInterviewResponse> =
            successResponse(responseCode = responseCode, data = response)

        return ResponseEntity.status(responseBody.code).body(responseBody)
    }

    @PostMapping("/{interviewId}/utterances")
    override fun submitInterviewUtterance(
        @PathVariable interviewId: String,
        @Valid @RequestBody request: SubmitInterviewUtteranceRequest,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<SuccessResponse<ProfileInterviewResponse>> {
        val command = SubmitInterviewUtteranceCommand(
            guardianId = userDetails.getId(),
            interviewId = interviewId,
            text = request.text,
            sttRawText = request.sttRawText,
        )
        val result: ProfileInterviewResult = submitInterviewUtteranceUseCase.execute(command)
        val response: ProfileInterviewResponse = profileInterviewResponse(result = result)
        val responseBody: SuccessResponse<ProfileInterviewResponse> = successResponse(responseCode = OK, data = response)

        return ResponseEntity.status(responseBody.code).body(responseBody)
    }
}
