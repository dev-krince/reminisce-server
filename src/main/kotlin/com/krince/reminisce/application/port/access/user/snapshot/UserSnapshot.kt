package com.krince.reminisce.application.port.access.user.snapshot

import com.krince.reminisce.domain.model.user.User
import com.krince.reminisce.domain.model.user.vo.UserId
import java.time.LocalDateTime

class UserSnapshot(
    val userId: UserId,
    val email: String,
    val nickname: String,
    val role: String,
    val createdDate: LocalDateTime,
    val modifiedDate: LocalDateTime,
) {
    companion object {
        fun from(user: User): UserSnapshot = UserSnapshot(
            userId = user.userId,
            email = user.email.value,
            nickname = user.nickname.value,
            role = user.role.value,
            createdDate = requireNotNull(user.createdDate),
            modifiedDate = requireNotNull(user.modifiedDate),
        )
    }
}
