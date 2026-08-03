package com.krince.reminisce.application.port.out.user

import com.krince.reminisce.domain.model.user.User
import com.krince.reminisce.domain.model.user.vo.AuthProvider
import com.krince.reminisce.domain.model.user.vo.Email
import com.krince.reminisce.domain.model.user.vo.UserId

interface LoadUserPort {
    fun findByEmail(email: Email): User?

    fun existsByEmail(email: Email): Boolean

    fun findByUserId(userId: UserId): User?

    fun findByProviderAndProviderId(provider: AuthProvider, providerId: String): User?
}
