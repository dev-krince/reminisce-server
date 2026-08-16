package com.krince.reminisce.application.port.out.tts

interface LoadTtsCachePort {
    fun findFileUrlByCacheKey(cacheKey: String): String?
}
