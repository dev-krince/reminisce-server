package com.krince.reminisce.application.port.out.user

import com.krince.reminisce.domain.model.user.User
import com.krince.reminisce.domain.model.user.vo.LoginId
import com.krince.reminisce.domain.model.user.vo.UserId

interface LoadUserPort {
    fun findByLoginId(loginId: LoginId): User?

    fun findByUserId(userId: UserId): User?
}