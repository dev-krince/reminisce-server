package com.krince.reminisce.domain.model.childconsent

import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.childconsent.vo.ConsentId
import com.krince.reminisce.domain.model.childconsent.vo.ConsentVersion
import com.krince.reminisce.domain.model.childconsent.vo.VerificationMethod
import com.krince.reminisce.shared.util.UuidGenerator
import java.time.LocalDateTime

class ChildConsent(
    val consentId: ConsentId,
    val childId: ChildId,
    val consentVersion: ConsentVersion,
    val verificationMethod: VerificationMethod,
    val consentedAt: LocalDateTime,
    val withdrawnAt: LocalDateTime? = null,
    val createdDate: LocalDateTime? = null,
    val modifiedDate: LocalDateTime? = null,
) {
    companion object {
        fun givenByAuthenticatedParent(
            childId: ChildId,
            consentVersion: ConsentVersion,
            consentedAt: LocalDateTime,
        ): ChildConsent = ChildConsent(
            consentId = ConsentId(UuidGenerator.generate()),
            childId = childId,
            consentVersion = consentVersion,
            verificationMethod = VerificationMethod.AUTHENTICATED_PARENT,
            consentedAt = consentedAt,
            withdrawnAt = null,
        )
    }
}
