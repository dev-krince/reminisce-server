package com.krince.reminisce.application.port.out.tts

import com.krince.reminisce.domain.model.ttscache.TtsCache

interface CommandTtsCachePort {
    fun save(ttsCache: TtsCache)
}
