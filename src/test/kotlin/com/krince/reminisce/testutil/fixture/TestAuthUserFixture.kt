package com.krince.reminisce.testutil.fixture

import com.krince.reminisce.application.port.out.auth.PasswordEncoderPort
import com.krince.reminisce.infra.adapter.out.persistence.user.UserRepository
import com.krince.reminisce.infra.adapter.out.persistence.user.entity.UserOrmEntity
import com.krince.reminisce.shared.util.UuidGenerator

class TestAuthUserFixture(
    private val userRepository: UserRepository,
    private val passwordEncoderPort: PasswordEncoderPort,
) {
    fun saveLocalUser(email: String, rawPassword: String, nickname: String = "홍길동"): String {
        val userId: String = UuidGenerator.generate()
        val entity = UserOrmEntity(
            userId = userId,
            email = email,
            password = passwordEncoderPort.encode(rawPassword),
            nickname = nickname,
            provider = LOCAL_PROVIDER,
            role = USER_ROLE,
        )
        userRepository.save(entity)

        return userId
    }

    companion object {
        private const val LOCAL_PROVIDER = "LOCAL"
        private const val USER_ROLE = "ROLE_USER"
    }
}
