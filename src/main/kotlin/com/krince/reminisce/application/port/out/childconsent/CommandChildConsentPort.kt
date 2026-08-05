package com.krince.reminisce.application.port.out.childconsent

import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.childconsent.ChildConsent

interface CommandChildConsentPort {
    fun save(consent: ChildConsent): ChildConsent

    fun deleteAllByChildIds(childIds: List<ChildId>)
}
