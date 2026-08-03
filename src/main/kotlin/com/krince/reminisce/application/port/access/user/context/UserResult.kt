package com.krince.reminisce.application.port.access.user.context

import com.krince.reminisce.domain.model.user.User
import java.time.LocalDateTime

class UserResult(
    val userId: String,
    val email: String,
    val nickname: String,
    val role: String,
    val createdDate: LocalDateTime,
    val modifiedDate: LocalDateTime,
) {
    companion object {
        fun from(user: User): UserResult = UserResult(
            userId = user.userId.value,
            email = requireNotNull(user.email).value,
            nickname = user.nickname.value,
            role = user.role.value,
            createdDate = requireNotNull(user.createdDate),
            modifiedDate = requireNotNull(user.modifiedDate),
        )
    }
}
