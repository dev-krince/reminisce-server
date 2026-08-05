package com.krince.reminisce.application.port.out.childconsent

import com.krince.reminisce.domain.model.child.vo.ChildId

interface LoadChildConsentPort {
    fun existsActiveByChildId(childId: ChildId): Boolean
}
