package com.krince.reminisce.infra.adapter.`in`.controller

import com.krince.reminisce.application.port.`in`.message.command.SubmitUtteranceCommand
import com.krince.reminisce.application.port.`in`.message.result.UtteranceResult
import com.krince.reminisce.application.port.`in`.message.usecase.SubmitUtteranceUseCase
import com.krince.reminisce.application.port.`in`.postactivity.command.SubmitCardOrderCommand
import com.krince.reminisce.application.port.`in`.postactivity.command.SubmitRetellingCommand
import com.krince.reminisce.application.port.`in`.postactivity.result.CardOrderResult
import com.krince.reminisce.application.port.`in`.postactivity.result.RetellingResult
import com.krince.reminisce.application.port.`in`.postactivity.usecase.SubmitCardOrderUseCase
import com.krince.reminisce.application.port.`in`.postactivity.usecase.SubmitRetellingUseCase
import com.krince.reminisce.application.port.`in`.report.command.GetSessionReportCommand
import com.krince.reminisce.application.port.`in`.report.result.SessionReportResult
import com.krince.reminisce.application.port.`in`.report.usecase.GetSessionReportUseCase
import com.krince.reminisce.application.port.`in`.speakingsession.command.AdvanceSpeakingSceneCommand
import com.krince.reminisce.application.port.`in`.speakingsession.command.GetResumableSessionsCommand
import com.krince.reminisce.application.port.`in`.speakingsession.command.GetSpeakingSessionViewCommand
import com.krince.reminisce.application.port.`in`.speakingsession.command.StartSpeakingSessionCommand
import com.krince.reminisce.application.port.`in`.speakingsession.result.SpeakingSessionResult
import com.krince.reminisce.application.port.`in`.speakingsession.result.SpeakingSessionSummaryResult
import com.krince.reminisce.application.port.`in`.speakingsession.result.SpeakingSessionViewResult
import com.krince.reminisce.application.port.`in`.speakingsession.usecase.AdvanceSpeakingSceneUseCase
import com.krince.reminisce.application.port.`in`.speakingsession.usecase.GetResumableSessionsUseCase
import com.krince.reminisce.application.port.`in`.speakingsession.usecase.GetSpeakingSessionViewUseCase
import com.krince.reminisce.application.port.`in`.speakingsession.usecase.StartSpeakingSessionUseCase
import com.krince.reminisce.infra.adapter.`in`.dto.message.request.SubmitUtteranceRequest
import com.krince.reminisce.infra.adapter.`in`.dto.message.response.UtteranceResponse
import com.krince.reminisce.infra.adapter.`in`.dto.message.response.utteranceResponse
import com.krince.reminisce.infra.adapter.`in`.dto.postactivity.request.SubmitCardOrderRequest
import com.krince.reminisce.infra.adapter.`in`.dto.postactivity.request.SubmitRetellingRequest
import com.krince.reminisce.infra.adapter.`in`.dto.postactivity.response.CardOrderResultResponse
import com.krince.reminisce.infra.adapter.`in`.dto.postactivity.response.RetellingResultResponse
import com.krince.reminisce.infra.adapter.`in`.dto.postactivity.response.cardOrderResultResponse
import com.krince.reminisce.infra.adapter.`in`.dto.postactivity.response.retellingResultResponse
import com.krince.reminisce.infra.adapter.`in`.dto.report.response.SessionReportResponse
import com.krince.reminisce.infra.adapter.`in`.dto.report.response.sessionReportResponse
import com.krince.reminisce.infra.adapter.`in`.dto.speakingsession.request.StartSpeakingSessionRequest
import com.krince.reminisce.infra.adapter.`in`.dto.speakingsession.response.SpeakingSessionResponse
import com.krince.reminisce.infra.adapter.`in`.dto.speakingsession.response.SpeakingSessionSummaryResponse
import com.krince.reminisce.infra.adapter.`in`.dto.speakingsession.response.SpeakingSessionViewResponse
import com.krince.reminisce.infra.adapter.`in`.dto.speakingsession.response.speakingSessionResponse
import com.krince.reminisce.infra.adapter.`in`.dto.speakingsession.response.speakingSessionSummaryResponses
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
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/api/speaking-sessions")
class SpeakingSessionControllerImpl(
    private val startSpeakingSessionUseCase: StartSpeakingSessionUseCase,
    private val getSpeakingSessionViewUseCase: GetSpeakingSessionViewUseCase,
    private val advanceSpeakingSceneUseCase: AdvanceSpeakingSceneUseCase,
    private val submitUtteranceUseCase: SubmitUtteranceUseCase,
    private val getResumableSessionsUseCase: GetResumableSessionsUseCase,
    private val submitCardOrderUseCase: SubmitCardOrderUseCase,
    private val submitRetellingUseCase: SubmitRetellingUseCase,
    private val getSessionReportUseCase: GetSessionReportUseCase,
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

    @PostMapping("/{sessionId}/utterances")
    override fun submitUtterance(
        @PathVariable sessionId: String,
        @Valid @RequestBody request: SubmitUtteranceRequest,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<SuccessResponse<UtteranceResponse>> {
        val command = SubmitUtteranceCommand(
            sessionId = sessionId,
            guardianId = userDetails.getId(),
            text = request.text,
            sttRawText = request.sttRawText,
        )
        val result: UtteranceResult = submitUtteranceUseCase.execute(command)
        val response: UtteranceResponse = utteranceResponse(result)
        val responseBody: SuccessResponse<UtteranceResponse> =
            successResponse(responseCode = CREATED, data = response)

        return ResponseEntity.status(responseBody.code).body(responseBody)
    }

    @GetMapping(params = ["childId"])
    override fun getResumableSessions(
        @RequestParam childId: String,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<SuccessResponse<List<SpeakingSessionSummaryResponse>>> {
        val command = GetResumableSessionsCommand(childId = childId, guardianId = userDetails.getId())
        val results: List<SpeakingSessionSummaryResult> = getResumableSessionsUseCase.execute(command)
        val responses: List<SpeakingSessionSummaryResponse> = speakingSessionSummaryResponses(results)
        val responseBody: SuccessResponse<List<SpeakingSessionSummaryResponse>> =
            successResponse(responseCode = OK, data = responses)

        return ResponseEntity.status(responseBody.code).body(responseBody)
    }

    @PostMapping("/{sessionId}/post-activity/card-order")
    override fun submitCardOrder(
        @PathVariable sessionId: String,
        @Valid @RequestBody request: SubmitCardOrderRequest,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<SuccessResponse<CardOrderResultResponse>> {
        val command = SubmitCardOrderCommand(
            sessionId = sessionId,
            guardianId = userDetails.getId(),
            order = request.order,
        )
        val result: CardOrderResult = submitCardOrderUseCase.execute(command)
        val response: CardOrderResultResponse = cardOrderResultResponse(result)
        val responseBody: SuccessResponse<CardOrderResultResponse> =
            successResponse(responseCode = OK, data = response)

        return ResponseEntity.status(responseBody.code).body(responseBody)
    }

    @PostMapping("/{sessionId}/post-activity/retelling")
    override fun submitRetelling(
        @PathVariable sessionId: String,
        @Valid @RequestBody request: SubmitRetellingRequest,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<SuccessResponse<RetellingResultResponse>> {
        val command = SubmitRetellingCommand(
            sessionId = sessionId,
            guardianId = userDetails.getId(),
            audio = request.audio,
        )
        val result: RetellingResult = submitRetellingUseCase.execute(command)
        val response: RetellingResultResponse = retellingResultResponse(result)
        val responseBody: SuccessResponse<RetellingResultResponse> =
            successResponse(responseCode = OK, data = response)

        return ResponseEntity.status(responseBody.code).body(responseBody)
    }

    @GetMapping("/{sessionId}/report")
    override fun getSessionReport(
        @PathVariable sessionId: String,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<SuccessResponse<SessionReportResponse>> {
        val command = GetSessionReportCommand(sessionId = sessionId, guardianId = userDetails.getId())
        val result: SessionReportResult = getSessionReportUseCase.execute(command)
        val response: SessionReportResponse = sessionReportResponse(result)
        val responseBody: SuccessResponse<SessionReportResponse> =
            successResponse(responseCode = OK, data = response)

        return ResponseEntity.status(responseBody.code).body(responseBody)
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
