package com.krince.reminisce.infra.adapter.out.persistence.postactivityresult

import com.krince.reminisce.application.port.out.postactivityresult.CommandPostActivityResultPort
import com.krince.reminisce.application.port.out.postactivityresult.LoadPostActivityResultPort
import com.krince.reminisce.domain.model.postactivityresult.PostActivityResult
import com.krince.reminisce.domain.model.speakingsession.vo.SpeakingSessionId
import com.krince.reminisce.infra.adapter.out.persistence.postactivityresult.entity.PostActivityResultOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.postactivityresult.mapper.PostActivityResultMapper
import org.springframework.stereotype.Component

@Component
class PostActivityResultOrmAdapter(
    private val repository: PostActivityResultRepository,
) : CommandPostActivityResultPort, LoadPostActivityResultPort {

    override fun save(result: PostActivityResult): PostActivityResult {
        val ormEntity: PostActivityResultOrmEntity = PostActivityResultMapper.toEntity(result)
        val savedEntity: PostActivityResultOrmEntity = repository.saveAndFlush(ormEntity)

        return PostActivityResultMapper.toDomain(savedEntity)
    }

    override fun findBySession(sessionId: SpeakingSessionId): PostActivityResult? {
        val ormEntity: PostActivityResultOrmEntity = repository.findBySessionId(sessionId.value) ?: return null

        return PostActivityResultMapper.toDomain(ormEntity)
    }

    override fun findRetellingAudioUrlsBySessionIds(sessionIds: List<String>): List<String> {
        if (sessionIds.isEmpty()) {
            return emptyList()
        }

        return repository.findRetellingAudioUrlsBySessionIdIn(sessionIds)
    }

    override fun deleteAllBySessionIds(sessionIds: List<String>) {
        if (sessionIds.isEmpty()) {
            return
        }

        repository.deleteAllBySessionIdIn(sessionIds)
    }
}
