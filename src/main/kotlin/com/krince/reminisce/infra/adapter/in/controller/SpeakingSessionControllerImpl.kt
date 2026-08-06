package com.krince.reminisce.infra.adapter.`in`.controller

import com.krince.reminisce.application.port.`in`.speakingsession.command.AdvanceSpeakingSceneCommand
import com.krince.reminisce.application.port.`in`.speakingsession.command.GetSpeakingSessionViewCommand
import com.krince.reminisce.application.port.`in`.speakingsession.command.StartSpeakingSessionCommand
import com.krince.reminisce.application.port.`in`.speakingsession.result.SpeakingSessionResult
import com.krince.reminisce.application.port.`in`.speakingsession.result.SpeakingSessionViewResult
import com.krince.reminisce.application.port.`in`.speakingsession.usecase.AdvanceSpeakingSceneUseCase
import com.krince.reminisce.application.port.`in`.speakingsession.usecase.GetSpeakingSessionViewUseCase
import com.krince.reminisce.application.port.`in`.speakingsession.usecase.StartSpeakingSessionUseCase
import com.krince.reminisce.infra.adapter.`in`.dto.speakingsession.request.StartSpeakingSessionRequest
import com.krince.reminisce.infra.adapter.`in`.dto.speakingsession.response.SpeakingSessionResponse
import com.krince.reminisce.infra.adapter.`in`.dto.speakingsession.response.SpeakingSessionViewResponse
import com.krince.reminisce.infra.adapter.`in`.dto.speakingsession.response.speakingSessionResponse
import com.krince.reminisce.infra.adapter.`in`.dto.speakingsession.response.speakingSessionViewResponse
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
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/api/speaking-sessions")
class SpeakingSessionControllerImpl(
    private val startSpeakingSessionUseCase: StartSpeakingSessionUseCase,
    private val getSpeakingSessionViewUseCase: GetSpeakingSessionViewUseCase,
    private val advanceSpeakingSceneUseCase: AdvanceSpeakingSceneUseCase,
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

    @GetMapping("/{sessionId}")
    override fun getSpeakingSessionView(
        @PathVariable sessionId: String,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<SuccessResponse<SpeakingSessionViewResponse>> {
        val command = GetSpeakingSessionViewCommand(sessionId = sessionId, guardianId = userDetails.getId())
        val result: SpeakingSessionViewResult = getSpeakingSessionViewUseCase.execute(command)

        return viewResponseEntity(result)
    }

    @PostMapping("/{sessionId}/advance")
    override fun advanceSpeakingScene(
        @PathVariable sessionId: String,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<SuccessResponse<SpeakingSessionViewResponse>> {
        val command = AdvanceSpeakingSceneCommand(sessionId = sessionId, guardianId = userDetails.getId())
        val result: SpeakingSessionViewResult = advanceSpeakingSceneUseCase.execute(command)

        return viewResponseEntity(result)
    }

    private fun viewResponseEntity(
        result: SpeakingSessionViewResult,
    ): ResponseEntity<SuccessResponse<SpeakingSessionViewResponse>> {
        val response: SpeakingSessionViewResponse = speakingSessionViewResponse(result = result)
        val responseBody: SuccessResponse<SpeakingSessionViewResponse> =
            successResponse(responseCode = OK, data = response)

        return ResponseEntity.status(responseBody.code).body(responseBody)
    }

    private fun resolveResponseCode(created: Boolean): SuccessResponseCode {
        if (created) {
            return CREATED
        }

        return OK
    }
}
