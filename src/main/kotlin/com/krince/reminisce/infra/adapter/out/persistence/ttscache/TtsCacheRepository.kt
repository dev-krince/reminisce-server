package com.krince.reminisce.infra.adapter.out.persistence.ttscache

import com.krince.reminisce.infra.adapter.out.persistence.ttscache.entity.TtsCacheOrmEntity
import org.springframework.data.jpa.repository.JpaRepository

interface TtsCacheRepository : JpaRepository<TtsCacheOrmEntity, String> {
    fun findFirstByCacheKey(cacheKey: String): TtsCacheOrmEntity?
}
