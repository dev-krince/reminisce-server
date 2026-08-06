package com.krince.reminisce.infra.adapter.out.persistence.speakingsession

import com.krince.reminisce.application.port.out.speakingsession.CommandSpeakingSessionPort
import com.krince.reminisce.application.port.out.speakingsession.LoadSpeakingSessionPort
import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.speakingsession.SpeakingSession
import com.krince.reminisce.domain.model.speakingsession.vo.SessionStatus
import com.krince.reminisce.domain.model.speakingsession.vo.SpeakingSessionId
import com.krince.reminisce.domain.model.story.vo.StoryId
import com.krince.reminisce.infra.adapter.out.persistence.speakingsession.entity.SpeakingSessionOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.speakingsession.mapper.SpeakingSessionMapper
import org.springframework.stereotype.Component

@Component
class SpeakingSessionOrmAdapter(
    private val repository: SpeakingSessionRepository,
) : CommandSpeakingSessionPort, LoadSpeakingSessionPort {

    override fun save(session: SpeakingSession): SpeakingSession {
        val ormEntity: SpeakingSessionOrmEntity = SpeakingSessionMapper.toEntity(session)
        val savedEntity: SpeakingSessionOrmEntity = repository.saveAndFlush(ormEntity)

        return SpeakingSessionMapper.toDomain(savedEntity)
    }

    override fun findInProgress(childId: ChildId, storyId: StoryId): SpeakingSession? {
        val ormEntity: SpeakingSessionOrmEntity = repository.findByChildIdAndStoryIdAndStatus(
            childId.value,
            storyId.value,
            SessionStatus.IN_PROGRESS.name,
        ) ?: return null

        return SpeakingSessionMapper.toDomain(ormEntity)
    }

    override fun findById(sessionId: SpeakingSessionId): SpeakingSession? {
        val ormEntity: SpeakingSessionOrmEntity = repository.findBySessionId(sessionId.value) ?: return null

        return SpeakingSessionMapper.toDomain(ormEntity)
    }

    override fun findInProgressByChild(childId: ChildId): List<SpeakingSession> =
        repository.findAllByChildIdAndStatusOrderByLastActivityAtDesc(
            childId.value,
            SessionStatus.IN_PROGRESS.name,
        ).map { SpeakingSessionMapper.toDomain(it) }
}
