package com.krince.reminisce.application.port.out.child

import com.krince.reminisce.domain.model.child.Child
import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.user.vo.UserId

interface LoadChildPort {
    fun findById(childId: ChildId): Child?

    fun findAllByGuardianId(guardianId: UserId): List<Child>

    fun countByGuardianId(guardianId: UserId): Long
}
