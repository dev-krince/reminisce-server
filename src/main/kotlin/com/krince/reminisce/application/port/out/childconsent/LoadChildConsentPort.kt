package com.krince.reminisce.application.port.out.childconsent

import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.childconsent.ChildConsent

interface LoadChildConsentPort {
    fun existsActiveByChildId(childId: ChildId): Boolean

    fun findActiveByChildId(childId: ChildId): ChildConsent?
}
