package com.krince.reminisce.infra.adapter.`in`.controller

import com.krince.reminisce.application.port.`in`.childconsent.command.WithdrawChildConsentCommand
import com.krince.reminisce.application.port.`in`.childconsent.usecase.WithdrawChildConsentUseCase
import com.krince.reminisce.infra.security.CustomUserDetails
import com.krince.reminisce.shared.response.SuccessResponseCode.NO_CONTENT
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/api/children/{childId}/consent")
class ConsentControllerImpl(
    private val withdrawChildConsentUseCase: WithdrawChildConsentUseCase,
) : ConsentController {

    @PostMapping("/withdrawal")
    override fun withdrawConsent(
        @PathVariable childId: String,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<Void> {
        val command = WithdrawChildConsentCommand(childId = childId, guardianId = userDetails.getId())
        withdrawChildConsentUseCase.execute(command)

        return ResponseEntity.status(NO_CONTENT.code).build()
    }
}
