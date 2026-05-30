package com.krince.boilerplate.application.port.access.user

import com.krince.boilerplate.application.port.access.user.snapshot.UserSnapshot
import com.krince.boilerplate.domain.model.user.vo.UserId

interface UserAccessPort {
    fun findByLoginId(loginId: String): UserSnapshot
    fun findByUserId(userId: UserId): UserSnapshot
}