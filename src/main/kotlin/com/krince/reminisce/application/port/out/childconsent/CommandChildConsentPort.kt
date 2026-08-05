package com.krince.reminisce.application.port.out.childconsent

import com.krince.reminisce.domain.model.childconsent.ChildConsent

interface CommandChildConsentPort {
    fun save(consent: ChildConsent): ChildConsent
}
