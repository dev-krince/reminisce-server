package com.krince.boilerplate.application.port.out.user

import com.krince.boilerplate.domain.model.user.User
import com.krince.boilerplate.domain.model.user.vo.LoginId
import com.krince.boilerplate.domain.model.user.vo.UserId

interface LoadUserPort {
    fun findByLoginId(loginId: LoginId): User?

    fun findByUserId(userId: UserId): User?
}