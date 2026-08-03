package com.krince.reminisce.infra.adapter.out.persistence.user

import com.krince.reminisce.infra.adapter.out.persistence.user.entity.UserOrmEntity
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<UserOrmEntity, String> {
    fun findByEmail(email: String): UserOrmEntity?

    fun existsByEmail(email: String): Boolean
}
