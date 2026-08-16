package com.krince.reminisce.application.port.out.profileinterview

import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.profileinterview.ProfileInterview

interface CommandProfileInterviewPort {
    fun save(interview: ProfileInterview): ProfileInterview

    fun deleteAllByChildIds(childIds: List<ChildId>)
}
