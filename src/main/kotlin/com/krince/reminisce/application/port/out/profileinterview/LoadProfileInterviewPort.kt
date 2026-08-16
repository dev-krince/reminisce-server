package com.krince.reminisce.application.port.out.profileinterview

import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.profileinterview.ProfileInterview
import com.krince.reminisce.domain.model.profileinterview.vo.ProfileInterviewId

interface LoadProfileInterviewPort {
    fun findById(interviewId: ProfileInterviewId): ProfileInterview?

    fun findInProgressByChild(childId: ChildId): ProfileInterview?

    fun findInterviewIdsByChildIds(childIds: List<ChildId>): List<String>
}
