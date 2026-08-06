package com.krince.reminisce.application.facade.user

import com.krince.reminisce.application.port.access.user.UserAccessPort
import com.krince.reminisce.application.port.access.user.snapshot.UserSnapshot
import com.krince.reminisce.application.port.out.user.LoadUserPort
import com.krince.reminisce.domain.model.user.User
import com.krince.reminisce.domain.model.user.vo.UserId
import com.krince.reminisce.shared.exception.NotFoundException
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND_USER
import org.springframework.stereotype.Service

@Service
class UserFacade(
    private val loadPort: LoadUserPort,
) : UserAccessPort {

    fun findById(userId: UserId): User = loadPort.findByUserId(userId)
        ?: throw NotFoundException(NOT_FOUND_USER, NOT_FOUND_USER.message)

    override fun findByUserId(userId: UserId): UserSnapshot {
        val user: User = loadPort.findByUserId(userId)
            ?: throw NotFoundException(NOT_FOUND_USER, NOT_FOUND_USER.message)

        return UserSnapshot.from(user)
    }
}
