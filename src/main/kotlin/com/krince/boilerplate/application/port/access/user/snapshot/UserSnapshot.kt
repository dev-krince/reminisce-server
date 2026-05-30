package com.krince.boilerplate.application.port.access.user.snapshot

import com.krince.boilerplate.domain.model.user.User
import com.krince.boilerplate.domain.model.user.vo.UserId
import java.time.LocalDateTime

class UserSnapshot(
    val userId: UserId,
    val loginId: String,
    val role: String,
    val createdDate: LocalDateTime,
    val modifiedDate: LocalDateTime,
) {
    companion object {
        fun from(user: User): UserSnapshot = UserSnapshot(
            userId = user.userId,
            loginId = user.loginId.value,
            role = user.role.value,
            createdDate = user.createdDate!!,
            modifiedDate = user.modifiedDate!!,
        )
    }
}