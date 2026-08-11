package com.krince.reminisce.domain.model.user

import com.krince.reminisce.domain.model.user.vo.AuthProvider
import com.krince.reminisce.domain.model.user.vo.Email
import com.krince.reminisce.domain.model.user.vo.Nickname
import com.krince.reminisce.domain.model.user.vo.Role
import com.krince.reminisce.domain.model.user.vo.UserId
import com.krince.reminisce.shared.util.UuidGenerator
import java.time.LocalDateTime

class User(
    val userId: UserId,
    val email: Email?,
    val nickname: Nickname,
    val provider: AuthProvider,
    val role: Role,
    val providerId: String? = null,
    val createdDate: LocalDateTime? = null,
    val modifiedDate: LocalDateTime? = null,
) {
    companion object {
        fun kakao(providerId: String, email: Email?, nickname: Nickname): User = User(
            userId = UserId(UuidGenerator.generate()),
            email = email,
            nickname = nickname,
            provider = AuthProvider.KAKAO,
            role = Role.user(),
            providerId = providerId,
        )

        fun google(providerId: String, email: Email?, nickname: Nickname): User = User(
            userId = UserId(UuidGenerator.generate()),
            email = email,
            nickname = nickname,
            provider = AuthProvider.GOOGLE,
            role = Role.user(),
            providerId = providerId,
        )

        fun naver(providerId: String, email: Email?, nickname: Nickname): User = User(
            userId = UserId(UuidGenerator.generate()),
            email = email,
            nickname = nickname,
            provider = AuthProvider.NAVER,
            role = Role.user(),
            providerId = providerId,
        )
    }
}
