package com.krince.reminisce.infra.adapter.out.persistence.ttscache

import com.krince.reminisce.application.port.out.tts.CommandTtsCachePort
import com.krince.reminisce.application.port.out.tts.LoadTtsCachePort
import com.krince.reminisce.domain.model.ttscache.TtsCache
import com.krince.reminisce.infra.adapter.out.persistence.ttscache.mapper.TtsCacheMapper
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Component

@Component
class TtsCacheOrmAdapter(
    private val repository: TtsCacheRepository,
) : LoadTtsCachePort, CommandTtsCachePort {

    override fun findFileUrlByCacheKey(cacheKey: String): String? =
        repository.findFirstByCacheKey(cacheKey)?.fileUrl

    override fun save(ttsCache: TtsCache) {
        runCatching { repository.save(TtsCacheMapper.toEntity(ttsCache)) }
            .recover { cause -> rethrowUnlessDuplicate(cause) }
    }

    private fun rethrowUnlessDuplicate(cause: Throwable) {
        if (cause is DataIntegrityViolationException) {
            return
        }

        throw cause
    }
}
