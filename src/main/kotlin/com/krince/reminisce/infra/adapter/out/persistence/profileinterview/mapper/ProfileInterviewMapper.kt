package com.krince.reminisce.infra.adapter.out.persistence.profileinterview.mapper

import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.profileinterview.InterviewMessage
import com.krince.reminisce.domain.model.profileinterview.ProfileInterview
import com.krince.reminisce.domain.model.profileinterview.vo.InterviewMessageId
import com.krince.reminisce.domain.model.profileinterview.vo.InterviewSpeaker
import com.krince.reminisce.domain.model.profileinterview.vo.InterviewStage
import com.krince.reminisce.domain.model.profileinterview.vo.ProfileInterviewId
import com.krince.reminisce.domain.model.profileinterview.vo.ProfileInterviewStatus
import com.krince.reminisce.infra.adapter.out.persistence.profileinterview.entity.InterviewMessageOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.profileinterview.entity.ProfileInterviewOrmEntity

object ProfileInterviewMapper {

    fun toEntity(interview: ProfileInterview): ProfileInterviewOrmEntity = ProfileInterviewOrmEntity(
        interviewId = interview.interviewId.value,
        childId = interview.childId.value,
        status = interview.status.name,
        currentStage = interview.currentStage.name,
        stageChildTurnCount = interview.stageChildTurnCount,
        totalChildTurnCount = interview.totalChildTurnCount,
        startedAt = interview.startedAt,
        lastActivityAt = interview.lastActivityAt,
    )

    fun toDomain(entity: ProfileInterviewOrmEntity): ProfileInterview = ProfileInterview(
        interviewId = ProfileInterviewId(entity.interviewId),
        childId = ChildId(entity.childId),
        status = ProfileInterviewStatus.valueOf(entity.status),
        currentStage = InterviewStage.valueOf(entity.currentStage),
        stageChildTurnCount = entity.stageChildTurnCount,
        totalChildTurnCount = entity.totalChildTurnCount,
        startedAt = entity.startedAt,
        lastActivityAt = entity.lastActivityAt,
    )

    fun toEntity(message: InterviewMessage): InterviewMessageOrmEntity = InterviewMessageOrmEntity(
        id = message.messageId.value,
        interviewId = message.interviewId.value,
        speaker = message.speaker.name,
        turnOrder = message.turnOrder,
        text = message.text,
        sttRawText = message.sttRawText,
        createdAt = message.createdAt,
    )

    fun toDomain(entity: InterviewMessageOrmEntity): InterviewMessage = InterviewMessage(
        messageId = InterviewMessageId(entity.id),
        interviewId = ProfileInterviewId(entity.interviewId),
        speaker = InterviewSpeaker.valueOf(entity.speaker),
        turnOrder = entity.turnOrder,
        text = entity.text,
        sttRawText = entity.sttRawText,
        createdAt = entity.createdAt,
    )
}
