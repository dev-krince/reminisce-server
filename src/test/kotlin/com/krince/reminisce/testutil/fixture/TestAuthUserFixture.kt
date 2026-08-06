package com.krince.reminisce.testutil.fixture

import com.krince.reminisce.infra.adapter.out.persistence.user.UserRepository
import com.krince.reminisce.infra.adapter.out.persistence.user.entity.UserOrmEntity
import com.krince.reminisce.shared.util.UuidGenerator

class TestAuthUserFixture(
    private val userRepository: UserRepository,
) {
    fun saveKakaoUser(providerId: String, nickname: String = "카카오"): String {
        val userId: String = UuidGenerator.generate()
        val entity = UserOrmEntity(
            userId = userId,
            email = null,
            nickname = nickname,
            provider = KAKAO_PROVIDER,
            role = USER_ROLE,
            providerId = providerId,
        )
        userRepository.save(entity)

        return userId
    }

    companion object {
        private const val KAKAO_PROVIDER = "KAKAO"
        private const val USER_ROLE = "ROLE_USER"
    }
}
