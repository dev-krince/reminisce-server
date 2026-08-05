package com.krince.reminisce.application.facade.childconsent

import com.krince.reminisce.application.port.access.childconsent.ChildConsentAccessPort
import com.krince.reminisce.application.port.out.childconsent.LoadChildConsentPort
import com.krince.reminisce.domain.model.child.vo.ChildId
import org.springframework.stereotype.Service

@Service
class ChildConsentAccessFacade(
    private val loadChildConsentPort: LoadChildConsentPort,
) : ChildConsentAccessPort {

    override fun hasActiveConsent(childId: ChildId): Boolean =
        loadChildConsentPort.existsActiveByChildId(childId)
}
