package com.krince.reminisce.application.facade.child

import com.krince.reminisce.application.port.access.child.ChildAccessPort
import com.krince.reminisce.application.port.out.child.LoadChildPort
import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.user.vo.UserId
import org.springframework.stereotype.Service

@Service
class ChildAccessFacade(
    private val loadChildPort: LoadChildPort,
) : ChildAccessPort {

    override fun findGuardianId(childId: ChildId): UserId? =
        loadChildPort.findById(childId)?.guardianId

    override fun findChildName(childId: ChildId): String? =
        loadChildPort.findById(childId)?.nickname?.value
}
