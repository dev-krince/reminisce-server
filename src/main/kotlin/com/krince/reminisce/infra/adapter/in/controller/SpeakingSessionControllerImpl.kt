package com.krince.reminisce.infra.adapter.`in`.controller

import com.krince.reminisce.application.port.`in`.message.command.SubmitUtteranceCommand
import com.krince.reminisce.application.port.`in`.message.result.UtteranceResult
import com.krince.reminisce.application.port.`in`.message.usecase.SubmitUtteranceUseCase
import com.krince.reminisce.application.port.`in`.mission.command.SubmitMissionAnswerCommand
import com.krince.reminisce.application.port.`in`.mission.result.MissionAnswerResult
import com.krince.reminisce.application.port.`in`.mission.usecase.SubmitMissionAnswerUseCase
import com.krince.reminisce.application.port.`in`.postactivity.command.SubmitCardOrderCommand
import com.krince.reminisce.application.port.`in`.postactivity.command.SubmitRetellingCommand
import com.krince.reminisce.application.port.`in`.postactivity.result.CardOrderResult
import com.krince.reminisce.application.port.`in`.postactivity.result.RetellingResult
import com.krince.reminisce.application.port.`in`.postactivity.usecase.SubmitCardOrderUseCase
import com.krince.reminisce.application.port.`in`.postactivity.usecase.SubmitRetellingUseCase
import com.krince.reminisce.application.port.`in`.report.command.GetLatestSessionReportCommand
import com.krince.reminisce.application.port.`in`.report.command.GetSessionReportCommand
import com.krince.reminisce.application.port.`in`.report.result.LatestSessionReportResult
import com.krince.reminisce.application.port.`in`.report.result.SessionReportResult
import com.krince.reminisce.application.port.`in`.report.usecase.GetLatestSessionReportUseCase
import com.krince.reminisce.application.port.`in`.report.usecase.GetSessionReportUseCase
import com.krince.reminisce.application.port.`in`.speakingsession.command.AdvanceSpeakingSceneCommand
import com.krince.reminisce.application.port.`in`.speakingsession.command.GetResumableSessionsCommand
import com.krince.reminisce.application.port.`in`.speakingsession.command.GetSpeakingSessionViewCommand
import com.krince.reminisce.application.port.`in`.speakingsession.command.GoBackSpeakingSceneCommand
import com.krince.reminisce.application.port.`in`.speakingsession.command.StartSpeakingSessionCommand
import com.krince.reminisce.application.port.`in`.speakingsession.command.DeleteSpeakingSessionCommand
import com.krince.reminisce.application.port.`in`.speakingsession.command.StopSpeakingSessionCommand
import com.krince.reminisce.application.port.`in`.speakingsession.result.SpeakingSessionResult
import com.krince.reminisce.application.port.`in`.speakingsession.result.SpeakingSessionSummaryResult
import com.krince.reminisce.application.port.`in`.speakingsession.result.SpeakingSessionViewResult
import com.krince.reminisce.application.port.`in`.speakingsession.usecase.AdvanceSpeakingSceneUseCase
import com.krince.reminisce.application.port.`in`.speakingsession.usecase.GetResumableSessionsUseCase
import com.krince.reminisce.application.port.`in`.speakingsession.usecase.GetSpeakingSessionViewUseCase
import com.krince.reminisce.application.port.`in`.speakingsession.usecase.GoBackSpeakingSceneUseCase
import com.krince.reminisce.application.port.`in`.speakingsession.usecase.StartSpeakingSessionUseCase
import com.krince.reminisce.application.port.`in`.speakingsession.usecase.DeleteSpeakingSessionUseCase
import com.krince.reminisce.application.port.`in`.speakingsession.usecase.StopSpeakingSessionUseCase
import com.krince.reminisce.application.port.`in`.speakingsession.command.GetSpeakingHintCommand
import com.krince.reminisce.application.port.`in`.speakingsession.result.SpeakingHintResult
import com.krince.reminisce.application.port.`in`.speakingsession.usecase.GetSpeakingHintUseCase
import com.krince.reminisce.application.port.out.file.StoreFilePort
import com.krince.reminisce.infra.adapter.`in`.dto.message.request.SubmitUtteranceRequest
import com.krince.reminisce.infra.adapter.`in`.dto.message.response.UtteranceResponse
import com.krince.reminisce.infra.adapter.`in`.dto.message.response.utteranceResponse
import com.krince.reminisce.infra.adapter.`in`.dto.mission.request.SubmitMissionAnswerRequest
import com.krince.reminisce.infra.adapter.`in`.dto.mission.response.MissionAnswerResponse
import com.krince.reminisce.infra.adapter.`in`.dto.mission.response.missionAnswerResponse
import com.krince.reminisce.infra.adapter.`in`.dto.postactivity.request.SubmitCardOrderRequest
import com.krince.reminisce.infra.adapter.`in`.dto.postactivity.request.SubmitRetellingRequest
import com.krince.reminisce.infra.adapter.`in`.dto.postactivity.response.CardOrderResultResponse
import com.krince.reminisce.infra.adapter.`in`.dto.postactivity.response.RetellingResultResponse
import com.krince.reminisce.infra.adapter.`in`.dto.postactivity.response.cardOrderResultResponse
import com.krince.reminisce.infra.adapter.`in`.dto.postactivity.response.retellingResultResponse
import com.krince.reminisce.infra.adapter.`in`.dto.report.response.LatestSessionReportResponse
import com.krince.reminisce.infra.adapter.`in`.dto.report.response.SessionReportResponse
import com.krince.reminisce.infra.adapter.`in`.dto.report.response.latestSessionReportResponse
import com.krince.reminisce.infra.adapter.`in`.dto.report.response.sessionReportResponse
import com.krince.reminisce.infra.adapter.`in`.dto.speakingsession.request.StartSpeakingSessionRequest
import com.krince.reminisce.infra.adapter.`in`.dto.speakingsession.response.SpeakingHintResponse
import com.krince.reminisce.infra.adapter.`in`.dto.speakingsession.response.SpeakingSessionResponse
import com.krince.reminisce.infra.adapter.`in`.dto.speakingsession.response.SpeakingSessionSummaryResponse
import com.krince.reminisce.infra.adapter.`in`.dto.speakingsession.response.SpeakingSessionViewResponse
import com.krince.reminisce.infra.adapter.`in`.dto.speakingsession.response.speakingHintResponse
import com.krince.reminisce.infra.adapter.`in`.dto.speakingsession.response.speakingSessionResponse
import com.krince.reminisce.infra.adapter.`in`.dto.speakingsession.response.speakingSessionSummaryResponses
import com.krince.reminisce.infra.adapter.`in`.dto.speakingsession.response.speakingSessionViewResponse
import com.krince.reminisce.infra.security.CustomUserDetails
import com.krince.reminisce.shared.response.SuccessResponse
import com.krince.reminisce.shared.response.SuccessResponseCode
import com.krince.reminisce.shared.response.SuccessResponseCode.CREATED
import com.krince.reminisce.shared.response.SuccessResponseCode.NO_CONTENT
import com.krince.reminisce.shared.response.SuccessResponseCode.OK
import com.krince.reminisce.shared.response.successResponse
import jakarta.validation.Valid
import org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@Validated
@RestController
@RequestMapping("/api/speaking-sessions")
class SpeakingSessionControllerImpl(
    private val startSpeakingSessionUseCase: StartSpeakingSessionUseCase,
    private val getSpeakingSessionViewUseCase: GetSpeakingSessionViewUseCase,
    private val getSpeakingHintUseCase: GetSpeakingHintUseCase,
    private val advanceSpeakingSceneUseCase: AdvanceSpeakingSceneUseCase,
    private val goBackSpeakingSceneUseCase: GoBackSpeakingSceneUseCase,
    private val stopSpeakingSessionUseCase: StopSpeakingSessionUseCase,
    private val deleteSpeakingSessionUseCase: DeleteSpeakingSessionUseCase,
    private val submitUtteranceUseCase: SubmitUtteranceUseCase,
    private val getResumableSessionsUseCase: GetResumableSessionsUseCase,
    private val submitCardOrderUseCase: SubmitCardOrderUseCase,
    private val submitMissionAnswerUseCase: SubmitMissionAnswerUseCase,
    private val submitRetellingUseCase: SubmitRetellingUseCase,
    private val getSessionReportUseCase: GetSessionReportUseCase,
    private val getLatestSessionReportUseCase: GetLatestSessionReportUseCase,
    private val storeFilePort: StoreFilePort,
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

    @GetMapping("/{sessionId}/hint")
    override fun getSpeakingHint(
        @PathVariable sessionId: String,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<SuccessResponse<SpeakingHintResponse>> {
        val command = GetSpeakingHintCommand(sessionId = sessionId, guardianId = userDetails.getId())
        val result: SpeakingHintResult = getSpeakingHintUseCase.execute(command)
        val responseBody: SuccessResponse<SpeakingHintResponse> =
            successResponse(responseCode = OK, data = speakingHintResponse(result))

        return ResponseEntity.status(responseBody.code).body(responseBody)
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

    @PostMapping("/{sessionId}/back")
    override fun goBackSpeakingScene(
        @PathVariable sessionId: String,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<SuccessResponse<SpeakingSessionViewResponse>> {
        val command = GoBackSpeakingSceneCommand(sessionId = sessionId, guardianId = userDetails.getId())
        val result: SpeakingSessionViewResult = goBackSpeakingSceneUseCase.execute(command)

        return viewResponseEntity(result)
    }

    @PostMapping("/{sessionId}/stop")
    override fun stopSpeakingSession(
        @PathVariable sessionId: String,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<SuccessResponse<SpeakingSessionResponse>> {
        val command = StopSpeakingSessionCommand(sessionId = sessionId, guardianId = userDetails.getId())
        val result: SpeakingSessionResult = stopSpeakingSessionUseCase.execute(command)
        val response: SpeakingSessionResponse = speakingSessionResponse(result = result)
        val responseBody: SuccessResponse<SpeakingSessionResponse> =
            successResponse(responseCode = OK, data = response)

        return ResponseEntity.status(responseBody.code).body(responseBody)
    }

    @DeleteMapping("/{sessionId}")
    override fun deleteSpeakingSession(
        @PathVariable sessionId: String,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<Void> {
        val command = DeleteSpeakingSessionCommand(guardianId = userDetails.getId(), sessionId = sessionId)
        deleteSpeakingSessionUseCase.execute(command)

        return ResponseEntity.status(NO_CONTENT.code).build()
    }

    @PostMapping("/{sessionId}/utterances", consumes = [MULTIPART_FORM_DATA_VALUE])
    override fun submitUtterance(
        @PathVariable sessionId: String,
        @Valid @RequestPart("request") request: SubmitUtteranceRequest,
        @RequestPart("audio", required = false) audio: MultipartFile?,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<SuccessResponse<UtteranceResponse>> {
        val utteranceAudioUrl: String? = audio?.let { storeFilePort.saveAudioOrThrows(it) }
        val command = SubmitUtteranceCommand(
            sessionId = sessionId,
            guardianId = userDetails.getId(),
            text = request.text,
            sttRawText = request.sttRawText,
            audioUrl = utteranceAudioUrl,
        )
        val result: UtteranceResult = try {
            submitUtteranceUseCase.execute(command)
        } catch (exception: Exception) {
            utteranceAudioUrl?.let { storeFilePort.deleteFile(it) }
            throw exception
        }
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

    @PostMapping("/{sessionId}/missions/{sceneId}/answer")
    override fun submitMissionAnswer(
        @PathVariable sessionId: String,
        @PathVariable sceneId: String,
        @Valid @RequestBody request: SubmitMissionAnswerRequest,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<SuccessResponse<MissionAnswerResponse>> {
        val command = SubmitMissionAnswerCommand(
            sessionId = sessionId,
            guardianId = userDetails.getId(),
            sceneId = sceneId,
            submittedOrder = request.submittedOrder,
            text = request.text,
        )
        val result: MissionAnswerResult = submitMissionAnswerUseCase.execute(command)
        val response: MissionAnswerResponse = missionAnswerResponse(result)
        val responseBody: SuccessResponse<MissionAnswerResponse> =
            successResponse(responseCode = OK, data = response)

        return ResponseEntity.status(responseBody.code).body(responseBody)
    }

    @PostMapping("/{sessionId}/post-activity/retelling", consumes = [MULTIPART_FORM_DATA_VALUE])
    override fun submitRetelling(
        @PathVariable sessionId: String,
        @Valid @RequestPart("request") request: SubmitRetellingRequest,
        @RequestPart("audio", required = false) audio: MultipartFile?,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<SuccessResponse<RetellingResultResponse>> {
        val retellingAudioUrl: String? = audio?.let { storeFilePort.saveAudioOrThrows(it) }
        val command = SubmitRetellingCommand(
            sessionId = sessionId,
            guardianId = userDetails.getId(),
            text = request.text,
            sceneSegments = request.sceneSegments,
            retellingAudioUrl = retellingAudioUrl,
        )
        val result: RetellingResult = try {
            submitRetellingUseCase.execute(command)
        } catch (exception: Exception) {
            retellingAudioUrl?.let { storeFilePort.deleteFile(it) }
            throw exception
        }
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

    @GetMapping("/reports/latest")
    override fun getLatestSessionReport(
        @RequestParam childId: String,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<SuccessResponse<LatestSessionReportResponse>> {
        val command = GetLatestSessionReportCommand(childId = childId, guardianId = userDetails.getId())
        val result: LatestSessionReportResult = getLatestSessionReportUseCase.execute(command)
        val response: LatestSessionReportResponse = latestSessionReportResponse(result = result)
        val responseBody: SuccessResponse<LatestSessionReportResponse> =
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
