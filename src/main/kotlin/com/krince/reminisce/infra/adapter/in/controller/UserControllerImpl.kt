package com.krince.reminisce.infra.adapter.`in`.controller

import com.krince.reminisce.application.port.access.user.context.UserResult
import com.krince.reminisce.application.port.`in`.user.command.GetUserCommand
import com.krince.reminisce.application.port.`in`.user.usecase.GetUserUseCase
import com.krince.reminisce.infra.adapter.`in`.dto.user.response.UserResponse
import com.krince.reminisce.infra.adapter.`in`.dto.user.response.userResponse
import com.krince.reminisce.shared.response.SuccessResponse
import com.krince.reminisce.shared.response.SuccessResponseCode.*
import com.krince.reminisce.shared.response.successResponse
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/api/users")
class UserControllerImpl(
    private val getUserUseCase: GetUserUseCase,
) : UserController {

    @GetMapping("/{userId}")
    override fun getUser(@PathVariable userId: String): ResponseEntity<SuccessResponse<UserResponse>> {
        val command = GetUserCommand(userId = userId)
        val result: UserResult = getUserUseCase.execute(command)
        val response: UserResponse = userResponse(userResult = result)
        val responseBody: SuccessResponse<UserResponse> = successResponse(responseCode = OK, data = response)

        return ResponseEntity.status(responseBody.code).body(responseBody)
    }
}