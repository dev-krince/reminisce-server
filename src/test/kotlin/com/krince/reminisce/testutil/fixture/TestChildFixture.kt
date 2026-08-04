package com.krince.reminisce.testutil.fixture

import com.krince.reminisce.infra.adapter.out.persistence.child.ChildRepository
import com.krince.reminisce.infra.adapter.out.persistence.child.entity.ChildOrmEntity
import org.springframework.stereotype.Component

@Component
class TestChildFixture(
    private val childRepository: ChildRepository,
) {
    fun saveChild(entity: ChildOrmEntity): ChildOrmEntity = childRepository.save(entity)

    fun findAllByGuardianId(guardianId: String): List<ChildOrmEntity> =
        childRepository.findAllByGuardianId(guardianId)

    fun countByGuardianId(guardianId: String): Long = childRepository.countByGuardianId(guardianId)

    fun deleteAllBatch() {
        childRepository.deleteAllInBatch()
    }
}
