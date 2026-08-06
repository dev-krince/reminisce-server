package com.krince.reminisce.infra.adapter.out.stt

import com.krince.reminisce.application.port.out.stt.SttPort
import org.springframework.stereotype.Component

@Component
class SttStubAdapter : SttPort {
    override fun transcribe(audio: String): String? {
        val trimmed: String = audio.trim()
        if (trimmed.isBlank()) {
            return null
        }

        return trimmed
    }
}
