package com.krince.reminisce.infra.adapter.out.persistence.childconsent.mapper

import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.childconsent.ChildConsent
import com.krince.reminisce.domain.model.childconsent.vo.ConsentId
import com.krince.reminisce.domain.model.childconsent.vo.ConsentVersion
import com.krince.reminisce.domain.model.childconsent.vo.VerificationMethod
import com.krince.reminisce.infra.adapter.out.persistence.childconsent.entity.ChildConsentOrmEntity

object ChildConsentMapper {
    fun toDomain(ormEntity: ChildConsentOrmEntity): ChildConsent = ChildConsent(
        consentId = ConsentId(ormEntity.consentId),
        childId = ChildId(ormEntity.childId),
        consentVersion = ConsentVersion(ormEntity.consentVersion),
        verificationMethod = VerificationMethod.valueOf(ormEntity.verificationMethod),
        consentedAt = ormEntity.consentedAt,
        withdrawnAt = ormEntity.withdrawnAt,
        createdDate = ormEntity.createdDate,
        modifiedDate = ormEntity.modifiedDate,
    )

    fun toEntity(domain: ChildConsent): ChildConsentOrmEntity = ChildConsentOrmEntity(
        consentId = domain.consentId.value,
        childId = domain.childId.value,
        consentVersion = domain.consentVersion.value,
        verificationMethod = domain.verificationMethod.name,
        consentedAt = domain.consentedAt,
        withdrawnAt = domain.withdrawnAt,
    ).apply {
        createdDate = domain.createdDate
        modifiedDate = domain.modifiedDate
    }
}
