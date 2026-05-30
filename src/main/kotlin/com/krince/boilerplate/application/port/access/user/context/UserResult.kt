package com.krince.boilerplate.application.port.access.user.context

import com.krince.boilerplate.domain.model.user.User
import java.time.LocalDateTime

class UserResult(
    val userId: String,
    val loginId: String,
    val role: String,
    val createdDate: LocalDateTime,
    val modifiedDate: LocalDateTime,
) {
    companion object {
        fun from(user: User): UserResult = UserResult(
            userId = user.userId.value,
            loginId = user.loginId.value,
            role = user.role.value,
            createdDate = user.createdDate!!,
            modifiedDate = user.modifiedDate!!,
        )
    }
}