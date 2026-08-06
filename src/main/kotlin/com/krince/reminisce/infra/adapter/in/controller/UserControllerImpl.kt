package com.krince.reminisce.infra.adapter.`in`.controller

import com.krince.reminisce.application.port.access.user.context.UserResult
import com.krince.reminisce.application.port.`in`.user.command.GetUserCommand
import com.krince.reminisce.application.port.`in`.user.command.WithdrawGuardianCommand
import com.krince.reminisce.application.port.`in`.user.usecase.GetUserUseCase
import com.krince.reminisce.application.port.`in`.user.usecase.WithdrawGuardianUseCase
import com.krince.reminisce.infra.adapter.`in`.dto.user.response.UserResponse
import com.krince.reminisce.infra.adapter.`in`.dto.user.response.userResponse
import com.krince.reminisce.infra.security.CustomUserDetails
import com.krince.reminisce.shared.response.SuccessResponse
import com.krince.reminisce.shared.response.SuccessResponseCode.NO_CONTENT
import com.krince.reminisce.shared.response.SuccessResponseCode.OK
import com.krince.reminisce.shared.response.successResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/api/users")
class UserControllerImpl(
    private val getUserUseCase: GetUserUseCase,
    private val withdrawGuardianUseCase: WithdrawGuardianUseCase,
) : UserController {

    @GetMapping("/me")
    override fun getUser(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<SuccessResponse<UserResponse>> {
        val command = GetUserCommand(userId = userDetails.getId())
        val result: UserResult = getUserUseCase.execute(command)
        val response: UserResponse = userResponse(userResult = result)
        val responseBody: SuccessResponse<UserResponse> = successResponse(responseCode = OK, data = response)

        return ResponseEntity.status(responseBody.code).body(responseBody)
    }

    @DeleteMapping("/me")
    override fun withdraw(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @RequestHeader(name = UserController.ACCESS_TOKEN_HEADER_NAME, required = false) accessToken: String?,
    ): ResponseEntity<Void> {
        val command = WithdrawGuardianCommand(userId = userDetails.getId(), accessToken = accessToken)
        withdrawGuardianUseCase.execute(command)

        return ResponseEntity.status(NO_CONTENT.code).build()
    }
}
