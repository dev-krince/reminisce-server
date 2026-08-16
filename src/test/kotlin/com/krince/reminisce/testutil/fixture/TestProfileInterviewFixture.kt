package com.krince.reminisce.testutil.fixture

import com.krince.reminisce.infra.adapter.out.persistence.profileinterview.InterviewMessageRepository
import com.krince.reminisce.infra.adapter.out.persistence.profileinterview.ProfileInterviewRepository
import com.krince.reminisce.infra.adapter.out.persistence.profileinterview.entity.InterviewMessageOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.profileinterview.entity.ProfileInterviewOrmEntity
import org.springframework.stereotype.Component

@Component
class TestProfileInterviewFixture(
    private val profileInterviewRepository: ProfileInterviewRepository,
    private val interviewMessageRepository: InterviewMessageRepository,
) {
    fun saveInterview(entity: ProfileInterviewOrmEntity): ProfileInterviewOrmEntity =
        profileInterviewRepository.save(entity)

    fun findAllInterviewsByChildId(childId: String): List<ProfileInterviewOrmEntity> =
        profileInterviewRepository.findAll().filter { it.childId == childId }

    fun findMessagesByInterviewId(interviewId: String): List<InterviewMessageOrmEntity> =
        interviewMessageRepository.findAllByInterviewIdOrderByTurnOrderAsc(interviewId)

    fun deleteAllBatch() {
        interviewMessageRepository.deleteAllInBatch()
        profileInterviewRepository.deleteAllInBatch()
    }
}
