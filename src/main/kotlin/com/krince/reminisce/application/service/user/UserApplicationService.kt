package com.krince.reminisce.application.service.user

import com.krince.reminisce.application.facade.user.UserFacade
import com.krince.reminisce.application.port.access.user.context.UserResult
import com.krince.reminisce.application.port.`in`.user.command.GetUserCommand
import com.krince.reminisce.application.port.`in`.user.usecase.GetUserUseCase
import com.krince.reminisce.domain.model.user.User
import com.krince.reminisce.domain.model.user.vo.UserId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserApplicationService(
    private val facade: UserFacade,
) : GetUserUseCase {

    @Transactional(readOnly = true)
    override fun execute(command: GetUserCommand): UserResult {
        val user: User = facade.findById(UserId(command.userId))

        return UserResult.from(user)
    }
}
