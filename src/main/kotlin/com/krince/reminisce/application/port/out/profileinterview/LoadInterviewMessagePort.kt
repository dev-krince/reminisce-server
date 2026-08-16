package com.krince.reminisce.application.port.out.profileinterview

import com.krince.reminisce.domain.model.profileinterview.InterviewMessage
import com.krince.reminisce.domain.model.profileinterview.vo.ProfileInterviewId

interface LoadInterviewMessagePort {
    fun countByInterview(interviewId: ProfileInterviewId): Long

    fun findLatestQumiMessage(interviewId: ProfileInterviewId): InterviewMessage?

    fun findRecentByInterview(interviewId: ProfileInterviewId, limit: Int): List<InterviewMessage>

    fun findAllByInterview(interviewId: ProfileInterviewId): List<InterviewMessage>
}
