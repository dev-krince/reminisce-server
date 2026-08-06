package com.krince.reminisce.infra.adapter.out.tts

import com.krince.reminisce.application.port.out.tts.TtsPort
import org.springframework.stereotype.Component

@Component
class TtsStubAdapter : TtsPort {

    companion object {
        private const val STUB_AUDIO_PREFIX = "stub://tts/"
    }

    override fun synthesize(text: String): String? {
        val trimmed: String = text.trim()
        if (trimmed.isBlank()) {
            return null
        }

        return "$STUB_AUDIO_PREFIX${trimmed.hashCode()}"
    }
}
