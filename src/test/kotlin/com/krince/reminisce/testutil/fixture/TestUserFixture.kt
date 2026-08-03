package com.krince.reminisce.testutil.fixture

import com.krince.reminisce.infra.adapter.out.persistence.user.UserRepository
import com.krince.reminisce.infra.adapter.out.persistence.user.entity.UserOrmEntity
import org.springframework.stereotype.Component

@Component
class TestUserFixture(
    private val userRepository: UserRepository,
) {
    fun saveUser(entity: UserOrmEntity): UserOrmEntity = userRepository.save(entity)

    fun findByEmail(email: String): UserOrmEntity? = userRepository.findByEmail(email)

    fun deleteAllBatch() {
        userRepository.deleteAllInBatch()
    }
}
