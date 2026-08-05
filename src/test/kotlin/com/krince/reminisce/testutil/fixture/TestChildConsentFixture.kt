package com.krince.reminisce.testutil.fixture

import com.krince.reminisce.infra.adapter.out.persistence.childconsent.ChildConsentRepository
import com.krince.reminisce.infra.adapter.out.persistence.childconsent.entity.ChildConsentOrmEntity
import org.springframework.stereotype.Component

@Component
class TestChildConsentFixture(
    private val childConsentRepository: ChildConsentRepository,
) {
    fun findAllByChildId(childId: String): List<ChildConsentOrmEntity> =
        childConsentRepository.findAllByChildId(childId)

    fun existsActiveByChildId(childId: String): Boolean =
        childConsentRepository.existsByChildIdAndWithdrawnAtIsNull(childId)

    fun count(): Long =
        childConsentRepository.count()

    fun deleteAllBatch() {
        childConsentRepository.deleteAllInBatch()
    }
}
