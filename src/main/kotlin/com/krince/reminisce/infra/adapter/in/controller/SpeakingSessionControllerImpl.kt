package com.krince.reminisce.infra.adapter.`in`.controller

import com.krince.reminisce.application.port.`in`.speakingsession.command.StartSpeakingSessionCommand
import com.krince.reminisce.application.port.`in`.speakingsession.result.SpeakingSessionResult
import com.krince.reminisce.application.port.`in`.speakingsession.usecase.StartSpeakingSessionUseCase
import com.krince.reminisce.infra.adapter.`in`.dto.speakingsession.request.StartSpeakingSessionRequest
import com.krince.reminisce.infra.adapter.`in`.dto.speakingsession.response.SpeakingSessionResponse
import com.krince.reminisce.infra.adapter.`in`.dto.speakingsession.response.speakingSessionResponse
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
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/api/speaking-sessions")
class SpeakingSessionControllerImpl(
    private val startSpeakingSessionUseCase: StartSpeakingSessionUseCase,
) : SpeakingSessionController {

    @PostMapping
    override fun startSpeakingSession(
        @Valid @RequestBody request: StartSpeakingSessionRequest,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<SuccessResponse<SpeakingSessionResponse>> {
        val command = StartSpeakingSessionCommand(
            guardianId = userDetails.getId(),
            childId = request.childId,
            storyId = request.storyId,
        )
        val result: SpeakingSessionResult = startSpeakingSessionUseCase.execute(command)
        val response: SpeakingSessionResponse = speakingSessionResponse(result = result)
        val responseCode: SuccessResponseCode = resolveResponseCode(result.created)
        val responseBody: SuccessResponse<SpeakingSessionResponse> =
            successResponse(responseCode = responseCode, data = response)

        return ResponseEntity.status(responseBody.code).body(responseBody)
    }

    private fun resolveResponseCode(created: Boolean): SuccessResponseCode {
        if (created) {
            return CREATED
        }

        return OK
    }
}
