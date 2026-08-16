package com.krince.reminisce.infra.adapter.out.persistence.ttscache.mapper

import com.krince.reminisce.domain.model.ttscache.TtsCache
import com.krince.reminisce.infra.adapter.out.persistence.ttscache.entity.TtsCacheOrmEntity

object TtsCacheMapper {
    fun toEntity(domain: TtsCache): TtsCacheOrmEntity = TtsCacheOrmEntity(
        cacheKey = domain.cacheKey,
        voiceProfile = domain.voiceProfile,
        fileUrl = domain.fileUrl,
    )
}
