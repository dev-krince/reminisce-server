package com.krince.reminisce.infra.adapter.out.persistence.user

import com.krince.reminisce.application.port.out.user.CommandUserPort
import com.krince.reminisce.application.port.out.user.LoadUserPort
import com.krince.reminisce.domain.model.user.User
import com.krince.reminisce.domain.model.user.vo.AuthProvider
import com.krince.reminisce.domain.model.user.vo.Email
import com.krince.reminisce.domain.model.user.vo.UserId
import com.krince.reminisce.infra.adapter.out.persistence.user.dto.UserAggregateEntity
import com.krince.reminisce.infra.adapter.out.persistence.user.entity.UserOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.user.mapper.UserMapper
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class UserOrmAdapter(
    private val repository: UserRepository,
) : LoadUserPort, CommandUserPort {

    override fun findByEmail(email: Email): User? {
        val userOrmEntity: UserOrmEntity = repository.findByEmail(email.value) ?: return null
        val userAggregateEntity = UserAggregateEntity(userOrmEntity = userOrmEntity)

        return UserMapper.toDomain(userAggregateEntity)
    }

    override fun existsByEmail(email: Email): Boolean = repository.existsByEmail(email.value)

    override fun findByUserId(userId: UserId): User? {
        val userOrmEntity: UserOrmEntity = repository.findByIdOrNull(userId.value) ?: return null
        val userAggregateEntity = UserAggregateEntity(userOrmEntity = userOrmEntity)

        return UserMapper.toDomain(userAggregateEntity)
    }

    override fun findByProviderAndProviderId(provider: AuthProvider, providerId: String): User? {
        val userOrmEntity: UserOrmEntity =
            repository.findByProviderAndProviderId(provider.name, providerId) ?: return null
        val userAggregateEntity = UserAggregateEntity(userOrmEntity = userOrmEntity)

        return UserMapper.toDomain(userAggregateEntity)
    }

    override fun save(user: User): User {
        val userOrmEntity: UserOrmEntity = UserMapper.toEntity(user).userOrmEntity
        val savedEntity: UserOrmEntity = repository.saveAndFlush(userOrmEntity)

        return UserMapper.toDomain(UserAggregateEntity(userOrmEntity = savedEntity))
    }
}
