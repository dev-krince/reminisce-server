package com.krince.reminisce.infra.adapter.out.persistence.postactivityresult

import com.krince.reminisce.infra.adapter.out.persistence.postactivityresult.entity.PostActivityResultOrmEntity
import org.springframework.data.jpa.repository.JpaRepository

interface PostActivityResultRepository : JpaRepository<PostActivityResultOrmEntity, String> {
    fun findBySessionId(sessionId: String): PostActivityResultOrmEntity?
}
