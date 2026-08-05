package com.krince.reminisce.application.port.access.childconsent

import com.krince.reminisce.domain.model.child.vo.ChildId

interface ChildConsentAccessPort {
    fun hasActiveConsent(childId: ChildId): Boolean
}
