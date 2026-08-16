package com.krince.reminisce.infra.adapter.out.persistence.profileinterview

import com.krince.reminisce.application.port.out.profileinterview.CommandInterviewMessagePort
import com.krince.reminisce.application.port.out.profileinterview.CommandProfileInterviewPort
import com.krince.reminisce.application.port.out.profileinterview.LoadInterviewMessagePort
import com.krince.reminisce.application.port.out.profileinterview.LoadProfileInterviewPort
import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.profileinterview.InterviewMessage
import com.krince.reminisce.domain.model.profileinterview.ProfileInterview
import com.krince.reminisce.domain.model.profileinterview.vo.InterviewSpeaker
import com.krince.reminisce.domain.model.profileinterview.vo.ProfileInterviewId
import com.krince.reminisce.domain.model.profileinterview.vo.ProfileInterviewStatus
import com.krince.reminisce.infra.adapter.out.persistence.profileinterview.entity.InterviewMessageOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.profileinterview.entity.ProfileInterviewOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.profileinterview.mapper.ProfileInterviewMapper
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class ProfileInterviewOrmAdapter(
    private val interviewRepository: ProfileInterviewRepository,
    private val messageRepository: InterviewMessageRepository,
) : LoadProfileInterviewPort, CommandProfileInterviewPort, LoadInterviewMessagePort, CommandInterviewMessagePort {

    override fun findById(interviewId: ProfileInterviewId): ProfileInterview? {
        val entity: ProfileInterviewOrmEntity = interviewRepository.findByIdOrNull(interviewId.value) ?: return null

        return ProfileInterviewMapper.toDomain(entity)
    }

    override fun findInProgressByChild(childId: ChildId): ProfileInterview? {
        val entity: ProfileInterviewOrmEntity = interviewRepository.findFirstByChildIdAndStatusOrderByStartedAtDesc(
            childId.value,
            ProfileInterviewStatus.IN_PROGRESS.name,
        ) ?: return null

        return ProfileInterviewMapper.toDomain(entity)
    }

    override fun findInterviewIdsByChildIds(childIds: List<ChildId>): List<String> {
        if (childIds.isEmpty()) {
            return emptyList()
        }

        return interviewRepository.findInterviewIdsByChildIdIn(childIds.map { it.value })
    }

    override fun save(interview: ProfileInterview): ProfileInterview {
        val savedEntity: ProfileInterviewOrmEntity =
            interviewRepository.saveAndFlush(ProfileInterviewMapper.toEntity(interview))

        return ProfileInterviewMapper.toDomain(savedEntity)
    }

    override fun deleteAllByChildIds(childIds: List<ChildId>) {
        if (childIds.isEmpty()) {
            return
        }

        interviewRepository.deleteAllByChildIdIn(childIds.map { it.value })
    }

    override fun countByInterview(interviewId: ProfileInterviewId): Long =
        messageRepository.countByInterviewId(interviewId.value)

    override fun findLatestQumiMessage(interviewId: ProfileInterviewId): InterviewMessage? {
        val entity: InterviewMessageOrmEntity = messageRepository.findFirstByInterviewIdAndSpeakerOrderByTurnOrderDesc(
            interviewId.value,
            InterviewSpeaker.QUMI.name,
        ) ?: return null

        return ProfileInterviewMapper.toDomain(entity)
    }

    override fun findRecentByInterview(interviewId: ProfileInterviewId, limit: Int): List<InterviewMessage> =
        messageRepository.findByInterviewIdOrderByTurnOrderDesc(interviewId.value, PageRequest.of(0, limit))
            .sortedBy { it.turnOrder }
            .map { ProfileInterviewMapper.toDomain(it) }

    override fun findAllByInterview(interviewId: ProfileInterviewId): List<InterviewMessage> =
        messageRepository.findAllByInterviewIdOrderByTurnOrderAsc(interviewId.value)
            .map { ProfileInterviewMapper.toDomain(it) }

    override fun save(message: InterviewMessage): InterviewMessage {
        val savedEntity: InterviewMessageOrmEntity =
            messageRepository.saveAndFlush(ProfileInterviewMapper.toEntity(message))

        return ProfileInterviewMapper.toDomain(savedEntity)
    }

    override fun deleteAllByInterviewIds(interviewIds: List<String>) {
        if (interviewIds.isEmpty()) {
            return
        }

        messageRepository.deleteAllByInterviewIdIn(interviewIds)
    }
}
