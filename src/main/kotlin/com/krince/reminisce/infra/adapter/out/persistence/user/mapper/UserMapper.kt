package com.krince.reminisce.infra.adapter.out.persistence.user.mapper

import com.krince.reminisce.domain.model.user.User
import com.krince.reminisce.domain.model.user.vo.LoginId
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
        loginId = LoginId(ormEntity.loginId),
        role = Role(ormEntity.role),
        createdDate = ormEntity.createdDate,
        modifiedDate = ormEntity.modifiedDate,
    )

    private fun toUserOrmEntity(domain: User): UserOrmEntity = UserOrmEntity(
        userId = domain.userId.value,
        loginId = domain.loginId.value,
        role = domain.role.value,
    ).apply {
        createdDate = domain.createdDate
        modifiedDate = domain.modifiedDate
    }
}