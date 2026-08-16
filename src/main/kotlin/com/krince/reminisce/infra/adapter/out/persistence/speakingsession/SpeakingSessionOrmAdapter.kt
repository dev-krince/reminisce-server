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

    override fun findResumableByChild(childId: ChildId): List<SpeakingSession> =
        repository.findAllByChildIdAndStatusInOrderByLastActivityAtDesc(
            childId.value,
            RESUMABLE_STATUS_NAMES,
        ).map { SpeakingSessionMapper.toDomain(it) }

    override fun findStartedStoryIdsByChild(childId: ChildId): List<String> =
        repository.findDistinctStoryIdsByChildId(childId.value)

    override fun findSessionIdsByChildIds(childIds: List<ChildId>): List<String> {
        if (childIds.isEmpty()) {
            return emptyList()
        }

        return repository.findSessionIdsByChildIdIn(childIds.map { it.value })
    }

    override fun deleteAllByChildIds(childIds: List<ChildId>) {
        if (childIds.isEmpty()) {
            return
        }

        repository.deleteAllByChildIdIn(childIds.map { it.value })
    }

    companion object {
        private val RESUMABLE_STATUS_NAMES: List<String> = listOf(
            SessionStatus.IN_PROGRESS.name,
            SessionStatus.POST_ACTIVITY.name,
        )
    }
}
