package com.krince.reminisce.application.port.access.user

import com.krince.reminisce.application.port.access.user.snapshot.UserSnapshot
import com.krince.reminisce.domain.model.user.vo.UserId

interface UserAccessPort {
    fun findByEmail(email: String): UserSnapshot
    fun findByUserId(userId: UserId): UserSnapshot
}
