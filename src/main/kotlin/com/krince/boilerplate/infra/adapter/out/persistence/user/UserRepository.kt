package com.krince.boilerplate.infra.adapter.out.persistence.user

import com.krince.boilerplate.infra.adapter.out.persistence.user.entity.UserOrmEntity
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<UserOrmEntity, String> {
    fun findByLoginId(loginId: String): UserOrmEntity?
}