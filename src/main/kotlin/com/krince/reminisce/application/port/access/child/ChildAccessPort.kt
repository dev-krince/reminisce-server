package com.krince.reminisce.application.port.access.child

import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.user.vo.UserId

interface ChildAccessPort {
    fun findGuardianId(childId: ChildId): UserId?

    fun findChildName(childId: ChildId): String?
}
