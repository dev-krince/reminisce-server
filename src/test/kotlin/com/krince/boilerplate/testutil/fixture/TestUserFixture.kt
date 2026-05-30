package com.krince.boilerplate.testutil.fixture

import com.krince.boilerplate.infra.adapter.out.persistence.user.UserRepository
import com.krince.boilerplate.infra.adapter.out.persistence.user.entity.UserOrmEntity
import org.springframework.stereotype.Component

@Component
class TestUserFixture(
    private val userRepository: UserRepository,
) {
    fun saveUser(entity: UserOrmEntity): UserOrmEntity = userRepository.save(entity)

    fun deleteAllBatch() {
        userRepository.deleteAllInBatch()
    }
}
