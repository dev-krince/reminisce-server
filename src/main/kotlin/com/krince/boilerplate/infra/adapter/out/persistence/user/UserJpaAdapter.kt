package com.krince.boilerplate.infra.adapter.out.persistence.user

import com.krince.boilerplate.application.port.out.user.LoadUserPort
import com.krince.boilerplate.domain.model.user.User
import com.krince.boilerplate.domain.model.user.vo.LoginId
import com.krince.boilerplate.domain.model.user.vo.UserId
import com.krince.boilerplate.infra.adapter.out.persistence.user.dto.UserAggregateEntity
import com.krince.boilerplate.infra.adapter.out.persistence.user.entity.UserOrmEntity
import com.krince.boilerplate.infra.adapter.out.persistence.user.mapper.UserMapper
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class UserJpaAdapter(
    private val repository: UserRepository
) : LoadUserPort {
    override fun findByLoginId(loginId: LoginId): User? {
        val userOrmEntity: UserOrmEntity = repository.findByLoginId(loginId.value) ?: return null
        val userAggregateEntity = UserAggregateEntity(userOrmEntity = userOrmEntity)

        return UserMapper.toDomain(userAggregateEntity)
    }

    override fun findByUserId(userId: UserId): User? {
        val userOrmEntity: UserOrmEntity = repository.findByIdOrNull(userId.value) ?: return null
        val userAggregateEntity = UserAggregateEntity(userOrmEntity = userOrmEntity)

        return UserMapper.toDomain(userAggregateEntity)
    }
}