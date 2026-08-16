package com.krince.reminisce.application.port.out.child

import com.krince.reminisce.domain.model.child.Child
import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.user.vo.UserId

interface CommandChildPort {
    fun save(child: Child): Child

    fun deleteById(childId: ChildId)

    fun deleteAllByGuardianId(guardianId: UserId)
}
