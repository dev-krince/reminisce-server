package com.krince.reminisce.infra.adapter.out.persistence.childconsent

import com.krince.reminisce.infra.adapter.out.persistence.childconsent.entity.ChildConsentOrmEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ChildConsentRepository : JpaRepository<ChildConsentOrmEntity, String> {
    fun existsByChildIdAndWithdrawnAtIsNull(childId: String): Boolean

    fun findFirstByChildIdAndWithdrawnAtIsNull(childId: String): ChildConsentOrmEntity?

    fun findAllByChildId(childId: String): List<ChildConsentOrmEntity>

    fun deleteAllByChildIdIn(childIds: List<String>)
}
