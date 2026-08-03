package com.krince.reminisce.infra.adapter.`in`.controller

import com.krince.reminisce.application.port.access.user.context.UserResult
import com.krince.reminisce.application.port.`in`.user.command.ConfirmEmailVerificationCommand
import com.krince.reminisce.application.port.`in`.user.command.GetUserCommand
import com.krince.reminisce.application.port.`in`.user.command.SendEmailVerificationCommand
import com.krince.reminisce.application.port.`in`.user.command.SignUpCommand
import com.krince.reminisce.application.port.`in`.user.usecase.ConfirmEmailVerificationUseCase
import com.krince.reminisce.application.port.`in`.user.usecase.GetUserUseCase
import com.krince.reminisce.application.port.`in`.user.usecase.SendEmailVerificationUseCase
import com.krince.reminisce.application.port.`in`.user.usecase.SignUpUseCase
import com.krince.reminisce.infra.adapter.`in`.dto.user.request.ConfirmEmailVerificationRequest
import com.krince.reminisce.infra.adapter.`in`.dto.user.request.SendEmailVerificationRequest
import com.krince.reminisce.infra.adapter.`in`.dto.user.request.SignUpRequest
import com.krince.reminisce.infra.adapter.`in`.dto.user.response.SignUpResponse
import com.krince.reminisce.infra.adapter.`in`.dto.user.response.UserResponse
import com.krince.reminisce.infra.adapter.`in`.dto.user.response.signUpResponse
import com.krince.reminisce.infra.adapter.`in`.dto.user.response.userResponse
import com.krince.reminisce.shared.response.SuccessResponse
import com.krince.reminisce.shared.response.SuccessResponseCode.CREATED
import com.krince.reminisce.shared.response.SuccessResponseCode.NO_CONTENT
import com.krince.reminisce.shared.response.SuccessResponseCode.OK
import com.krince.reminisce.shared.response.successResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/api/users")
class UserControllerImpl(
    private val signUpUseCase: SignUpUseCase,
    private val sendEmailVerificationUseCase: SendEmailVerificationUseCase,
    private val confirmEmailVerificationUseCase: ConfirmEmailVerificationUseCase,
    private val getUserUseCase: GetUserUseCase,
) : UserController {

    @PostMapping
    override fun signUp(@Valid @RequestBody request: SignUpRequest): ResponseEntity<SuccessResponse<SignUpResponse>> {
        val command = SignUpCommand(email = request.email, password = request.password, nickname = request.nickname)
        val result: UserResult = signUpUseCase.execute(command)
        val response: SignUpResponse = signUpResponse(userResult = result)
        val responseBody: SuccessResponse<SignUpResponse> = successResponse(responseCode = CREATED, data = response)

        return ResponseEntity.status(responseBody.code).body(responseBody)
    }

    @PostMapping("/email-verifications")
    override fun sendEmailVerification(
        @Valid @RequestBody request: SendEmailVerificationRequest,
    ): ResponseEntity<Void> {
        val command = SendEmailVerificationCommand(email = request.email)
        sendEmailVerificationUseCase.execute(command)

        return ResponseEntity.status(NO_CONTENT.code).build()
    }

    @PostMapping("/email-verifications/confirm")
    override fun confirmEmailVerification(
        @Valid @RequestBody request: ConfirmEmailVerificationRequest,
    ): ResponseEntity<Void> {
        val command = ConfirmEmailVerificationCommand(email = request.email, code = request.code)
        confirmEmailVerificationUseCase.execute(command)

        return ResponseEntity.status(NO_CONTENT.code).build()
    }

    @GetMapping("/{userId}")
    override fun getUser(@PathVariable userId: String): ResponseEntity<SuccessResponse<UserResponse>> {
        val command = GetUserCommand(userId = userId)
        val result: UserResult = getUserUseCase.execute(command)
        val response: UserResponse = userResponse(userResult = result)
        val responseBody: SuccessResponse<UserResponse> = successResponse(responseCode = OK, data = response)

        return ResponseEntity.status(responseBody.code).body(responseBody)
    }
}
