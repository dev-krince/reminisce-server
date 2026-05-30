package com.krince.boilerplate.application.facade.user

import com.krince.boilerplate.application.port.access.user.UserAccessPort
import com.krince.boilerplate.application.port.access.user.snapshot.UserSnapshot
import com.krince.boilerplate.application.port.out.user.LoadUserPort
import com.krince.boilerplate.domain.model.user.User
import com.krince.boilerplate.domain.model.user.vo.LoginId
import com.krince.boilerplate.domain.model.user.vo.UserId
import com.krince.boilerplate.shared.exception.NotFoundException
import com.krince.boilerplate.shared.response.ExceptionResponseCode.*
import org.springframework.stereotype.Service

@Service
class UserFacade(
//    private val commandPort: CommandUserPort,
    private val loadPort: LoadUserPort,
) : UserAccessPort {

    fun findById(userId: UserId): User = loadPort.findByUserId(userId)
        ?: throw NotFoundException(NOT_FOUND_USER, NOT_FOUND_USER.message)

    override fun findByLoginId(loginId: String): UserSnapshot {
        val user: User = loadPort.findByLoginId(LoginId(loginId))
            ?: throw NotFoundException(NOT_FOUND_USER, NOT_FOUND_USER.message)

        return UserSnapshot.from(user)
    }

    override fun findByUserId(userId: UserId): UserSnapshot {
        val user: User = loadPort.findByUserId(userId)
            ?: throw NotFoundException(NOT_FOUND_USER, NOT_FOUND_USER.message)

        return UserSnapshot.from(user)
    }
}