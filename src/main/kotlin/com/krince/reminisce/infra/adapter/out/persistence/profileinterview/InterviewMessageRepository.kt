package com.krince.reminisce.infra.adapter.out.persistence.profileinterview

import com.krince.reminisce.infra.adapter.out.persistence.profileinterview.entity.InterviewMessageOrmEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface InterviewMessageRepository : JpaRepository<InterviewMessageOrmEntity, String> {
    fun countByInterviewId(interviewId: String): Long

    fun findFirstByInterviewIdAndSpeakerOrderByTurnOrderDesc(
        interviewId: String,
        speaker: String,
    ): InterviewMessageOrmEntity?

    fun findByInterviewIdOrderByTurnOrderDesc(interviewId: String, pageable: Pageable): List<InterviewMessageOrmEntity>

    fun findAllByInterviewIdOrderByTurnOrderAsc(interviewId: String): List<InterviewMessageOrmEntity>

    fun deleteAllByInterviewIdIn(interviewIds: List<String>)
}
