package com.krince.reminisce.infra.adapter.out.persistence.childconsent

import com.krince.reminisce.application.port.out.childconsent.CommandChildConsentPort
import com.krince.reminisce.application.port.out.childconsent.LoadChildConsentPort
import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.childconsent.ChildConsent
import com.krince.reminisce.infra.adapter.out.persistence.childconsent.entity.ChildConsentOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.childconsent.mapper.ChildConsentMapper
import org.springframework.stereotype.Component

@Component
class ChildConsentOrmAdapter(
    private val repository: ChildConsentRepository,
) : LoadChildConsentPort, CommandChildConsentPort {

    override fun save(consent: ChildConsent): ChildConsent {
        val childConsentOrmEntity: ChildConsentOrmEntity = ChildConsentMapper.toEntity(consent)
        val savedEntity: ChildConsentOrmEntity = repository.saveAndFlush(childConsentOrmEntity)

        return ChildConsentMapper.toDomain(savedEntity)
    }

    override fun existsActiveByChildId(childId: ChildId): Boolean =
        repository.existsByChildIdAndWithdrawnAtIsNull(childId.value)

    override fun findActiveByChildId(childId: ChildId): ChildConsent? =
        repository.findFirstByChildIdAndWithdrawnAtIsNull(childId.value)
            ?.let { ChildConsentMapper.toDomain(it) }

    override fun deleteAllByChildIds(childIds: List<ChildId>) {
        repository.deleteAllByChildIdIn(childIds.map { it.value })
    }
}
