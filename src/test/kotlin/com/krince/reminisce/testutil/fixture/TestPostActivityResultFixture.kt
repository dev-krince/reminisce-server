package com.krince.reminisce.testutil.fixture

import com.krince.reminisce.infra.adapter.out.persistence.postactivityresult.PostActivityResultRepository
import com.krince.reminisce.infra.adapter.out.persistence.postactivityresult.entity.PostActivityResultOrmEntity
import org.springframework.stereotype.Component

@Component
class TestPostActivityResultFixture(
    private val postActivityResultRepository: PostActivityResultRepository,
) {
    fun save(entity: PostActivityResultOrmEntity): PostActivityResultOrmEntity =
        postActivityResultRepository.save(entity)

    fun findBySessionId(sessionId: String): PostActivityResultOrmEntity? =
        postActivityResultRepository.findBySessionId(sessionId)

    fun count(): Long = postActivityResultRepository.count()

    fun deleteAllBatch() {
        postActivityResultRepository.deleteAllInBatch()
    }
}
