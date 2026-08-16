package com.krince.reminisce.domain.model.profileinterview

import com.krince.reminisce.domain.model.profileinterview.vo.InterviewMessageId
import com.krince.reminisce.domain.model.profileinterview.vo.InterviewSpeaker
import com.krince.reminisce.domain.model.profileinterview.vo.ProfileInterviewId
import com.krince.reminisce.shared.util.UuidGenerator
import java.time.LocalDateTime

class InterviewMessage(
    val messageId: InterviewMessageId,
    val interviewId: ProfileInterviewId,
    val speaker: InterviewSpeaker,
    val turnOrder: Long,
    val text: String,
    val sttRawText: String?,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun qumiLine(
            interviewId: ProfileInterviewId,
            turnOrder: Long,
            text: String,
            at: LocalDateTime,
        ): InterviewMessage = InterviewMessage(
            messageId = InterviewMessageId(UuidGenerator.generate()),
            interviewId = interviewId,
            speaker = InterviewSpeaker.QUMI,
            turnOrder = turnOrder,
            text = text,
            sttRawText = null,
            createdAt = at,
        )

        fun childUtterance(
            interviewId: ProfileInterviewId,
            turnOrder: Long,
            text: String,
            sttRawText: String?,
            at: LocalDateTime,
        ): InterviewMessage = InterviewMessage(
            messageId = InterviewMessageId(UuidGenerator.generate()),
            interviewId = interviewId,
            speaker = InterviewSpeaker.CHILD,
            turnOrder = turnOrder,
            text = text,
            sttRawText = sttRawText,
            createdAt = at,
        )
    }
}
