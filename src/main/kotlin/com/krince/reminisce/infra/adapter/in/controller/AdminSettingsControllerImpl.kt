package com.krince.reminisce.infra.adapter.`in`.controller

import com.krince.reminisce.application.port.`in`.admin.command.UpdateInterviewStageTurnsCommand
import com.krince.reminisce.application.port.`in`.admin.command.UpdateSceneTurnsCommand
import com.krince.reminisce.application.port.`in`.admin.result.InterviewStageTurnsResult
import com.krince.reminisce.application.port.`in`.admin.result.SceneTurnsResult
import com.krince.reminisce.application.port.`in`.admin.usecase.GetInterviewStageTurnsUseCase
import com.krince.reminisce.application.port.`in`.admin.usecase.UpdateInterviewStageTurnsUseCase
import com.krince.reminisce.application.port.`in`.admin.usecase.UpdateSceneTurnsUseCase
import com.krince.reminisce.domain.model.profileinterview.vo.InterviewStage
import com.krince.reminisce.infra.adapter.`in`.dto.admin.request.UpdateInterviewStageTurnsRequest
import com.krince.reminisce.infra.adapter.`in`.dto.admin.request.UpdateSceneTurnsRequest
import com.krince.reminisce.infra.adapter.`in`.dto.admin.response.InterviewStageTurnsResponse
import com.krince.reminisce.infra.adapter.`in`.dto.admin.response.SceneTurnsResponse
import com.krince.reminisce.infra.adapter.`in`.dto.admin.response.interviewStageTurnsResponse
import com.krince.reminisce.infra.adapter.`in`.dto.admin.response.sceneTurnsResponse
import com.krince.reminisce.shared.response.SuccessResponse
import com.krince.reminisce.shared.response.SuccessResponseCode.OK
import com.krince.reminisce.shared.response.successResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/api/admin")
class AdminSettingsControllerImpl(
    private val getInterviewStageTurnsUseCase: GetInterviewStageTurnsUseCase,
    private val updateInterviewStageTurnsUseCase: UpdateInterviewStageTurnsUseCase,
    private val updateSceneTurnsUseCase: UpdateSceneTurnsUseCase,
) : AdminSettingsController {

    @GetMapping("/interview-stage-turns")
    override fun getInterviewStageTurns(): ResponseEntity<SuccessResponse<InterviewStageTurnsResponse>> {
        val result: InterviewStageTurnsResult = getInterviewStageTurnsUseCase.execute()
        val response: InterviewStageTurnsResponse = interviewStageTurnsResponse(result)
        val responseBody: SuccessResponse<InterviewStageTurnsResponse> =
            successResponse(responseCode = OK, data = response)

        return ResponseEntity.status(responseBody.code).body(responseBody)
    }

    @PutMapping("/interview-stage-turns")
    override fun updateInterviewStageTurns(
        @Valid @RequestBody request: UpdateInterviewStageTurnsRequest,
    ): ResponseEntity<SuccessResponse<InterviewStageTurnsResponse>> {
        val command = UpdateInterviewStageTurnsCommand(
            adminKey = request.adminKey,
            stageTurns = mapOf(
                InterviewStage.FREE_TALK to request.freeTalk,
                InterviewStage.EXPERIENCE to request.experience,
                InterviewStage.STORY_LISTENING to request.storyListening,
                InterviewStage.CHARACTER_FEELING to request.characterFeeling,
                InterviewStage.STORY_CONTINUATION to request.storyContinuation,
                InterviewStage.CHILD_QUESTION to request.childQuestion,
            ),
        )
        val result: InterviewStageTurnsResult = updateInterviewStageTurnsUseCase.execute(command)
        val response: InterviewStageTurnsResponse = interviewStageTurnsResponse(result)
        val responseBody: SuccessResponse<InterviewStageTurnsResponse> =
            successResponse(responseCode = OK, data = response)

        return ResponseEntity.status(responseBody.code).body(responseBody)
    }

    @PatchMapping("/scenes/{sceneId}/turns")
    override fun updateSceneTurns(
        @PathVariable sceneId: String,
        @Valid @RequestBody request: UpdateSceneTurnsRequest,
    ): ResponseEntity<SuccessResponse<SceneTurnsResponse>> {
        val command = UpdateSceneTurnsCommand(
            adminKey = request.adminKey,
            sceneId = sceneId,
            preferredTurns = request.preferredTurns,
            maxTurns = request.maxTurns,
        )
        val result: SceneTurnsResult = updateSceneTurnsUseCase.execute(command)
        val response: SceneTurnsResponse = sceneTurnsResponse(result)
        val responseBody: SuccessResponse<SceneTurnsResponse> = successResponse(responseCode = OK, data = response)

        return ResponseEntity.status(responseBody.code).body(responseBody)
    }
}
