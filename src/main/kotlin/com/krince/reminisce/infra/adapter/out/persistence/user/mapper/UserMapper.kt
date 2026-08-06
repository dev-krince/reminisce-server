package com.krince.reminisce.infra.adapter.out.persistence.user.mapper

import com.krince.reminisce.domain.model.user.User
import com.krince.reminisce.domain.model.user.vo.AuthProvider
import com.krince.reminisce.domain.model.user.vo.Email
import com.krince.reminisce.domain.model.user.vo.Nickname
import com.krince.reminisce.domain.model.user.vo.Role
import com.krince.reminisce.domain.model.user.vo.UserId
import com.krince.reminisce.infra.adapter.out.persistence.user.dto.UserAggregateEntity
import com.krince.reminisce.infra.adapter.out.persistence.user.entity.UserOrmEntity

object UserMapper {
    fun toDomain(aggregateEntity: UserAggregateEntity): User {
        val user: User = toUser(aggregateEntity.userOrmEntity)

        return user
    }

    fun toEntity(user: User): UserAggregateEntity = UserAggregateEntity(
        userOrmEntity = toUserOrmEntity(user)
    )

    private fun toUser(ormEntity: UserOrmEntity): User = User(
        userId = UserId(ormEntity.userId),
        email = ormEntity.email?.let { Email(it) },
        nickname = Nickname(ormEntity.nickname),
        provider = AuthProvider.valueOf(ormEntity.provider),
        role = Role(ormEntity.role),
        providerId = ormEntity.providerId,
        createdDate = ormEntity.createdDate,
        modifiedDate = ormEntity.modifiedDate,
    )

    private fun toUserOrmEntity(domain: User): UserOrmEntity = UserOrmEntity(
        userId = domain.userId.value,
        email = domain.email?.value,
        nickname = domain.nickname.value,
        provider = domain.provider.name,
        role = domain.role.value,
        providerId = domain.providerId,
    ).apply {
        createdDate = domain.createdDate
        modifiedDate = domain.modifiedDate
    }
}
