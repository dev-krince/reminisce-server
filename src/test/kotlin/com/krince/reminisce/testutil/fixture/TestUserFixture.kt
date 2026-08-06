package com.krince.reminisce.testutil.fixture

import com.krince.reminisce.infra.adapter.out.persistence.user.UserRepository
import com.krince.reminisce.infra.adapter.out.persistence.user.entity.UserOrmEntity
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class TestUserFixture(
    private val userRepository: UserRepository,
) {
    fun saveUser(entity: UserOrmEntity): UserOrmEntity = userRepository.save(entity)

    fun findById(userId: String): UserOrmEntity? = userRepository.findByIdOrNull(userId)

    fun existsById(userId: String): Boolean = userRepository.existsById(userId)

    fun deleteAllBatch() {
        userRepository.deleteAllInBatch()
    }
}
