package com.krince.reminisce.infra.adapter.out.persistence.child

import com.krince.reminisce.infra.adapter.out.persistence.child.entity.ChildOrmEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ChildRepository : JpaRepository<ChildOrmEntity, String> {
    fun findAllByGuardianId(guardianId: String): List<ChildOrmEntity>

    fun countByGuardianId(guardianId: String): Long

    fun deleteAllByGuardianId(guardianId: String)
}
