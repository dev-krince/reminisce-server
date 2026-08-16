package com.krince.reminisce.application.port.out.profileinterview

import com.krince.reminisce.domain.model.profileinterview.InterviewMessage

interface CommandInterviewMessagePort {
    fun save(message: InterviewMessage): InterviewMessage

    fun deleteAllByInterviewIds(interviewIds: List<String>)
}
