package com.krince.reminisce.infra.adapter.`in`.controller

import com.krince.reminisce.application.port.`in`.child.command.GetChildCommand
import com.krince.reminisce.application.port.`in`.child.command.GetChildrenCommand
import com.krince.reminisce.application.port.`in`.child.command.RegisterChildCommand
import com.krince.reminisce.application.port.`in`.child.result.ChildResult
import com.krince.reminisce.application.port.`in`.child.usecase.GetChildUseCase
import com.krince.reminisce.application.port.`in`.child.usecase.GetChildrenUseCase
import com.krince.reminisce.application.port.`in`.child.usecase.RegisterChildUseCase
import com.krince.reminisce.infra.adapter.`in`.dto.child.request.RegisterChildRequest
import com.krince.reminisce.infra.adapter.`in`.dto.child.response.ChildResponse
import com.krince.reminisce.infra.adapter.`in`.dto.child.response.childResponse
import com.krince.reminisce.infra.security.CustomUserDetails
import com.krince.reminisce.shared.response.SuccessResponse
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
@RequestMapping("/api/children")
class ChildControllerImpl(
    private val registerChildUseCase: RegisterChildUseCase,
    private val getChildrenUseCase: GetChildrenUseCase,
    private val getChildUseCase: GetChildUseCase,
) : ChildController {

    @PostMapping
    override fun registerChild(
        @Valid @RequestBody request: RegisterChildRequest,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<SuccessResponse<ChildResponse>> {
        val command = RegisterChildCommand(
            guardianId = userDetails.getId(),
            nickname = request.nickname,
            birthYear = request.birthYear,
        )
        val result: ChildResult = registerChildUseCase.execute(command)
        val response: ChildResponse = childResponse(childResult = result)
        val responseBody: SuccessResponse<ChildResponse> = successResponse(responseCode = CREATED, data = response)

        return ResponseEntity.status(responseBody.code).body(responseBody)
    }

    @GetMapping
    override fun getChildren(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<SuccessResponse<List<ChildResponse>>> {
        val command = GetChildrenCommand(guardianId = userDetails.getId())
        val results: List<ChildResult> = getChildrenUseCase.execute(command)
        val response: List<ChildResponse> = results.map { childResponse(childResult = it) }
        val responseBody: SuccessResponse<List<ChildResponse>> = successResponse(responseCode = OK, data = response)

        return ResponseEntity.status(responseBody.code).body(responseBody)
    }

    @GetMapping("/{childId}")
    override fun getChild(
        @PathVariable childId: String,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<SuccessResponse<ChildResponse>> {
        val command = GetChildCommand(guardianId = userDetails.getId(), childId = childId)
        val result: ChildResult = getChildUseCase.execute(command)
        val response: ChildResponse = childResponse(childResult = result)
        val responseBody: SuccessResponse<ChildResponse> = successResponse(responseCode = OK, data = response)

        return ResponseEntity.status(responseBody.code).body(responseBody)
    }
}
