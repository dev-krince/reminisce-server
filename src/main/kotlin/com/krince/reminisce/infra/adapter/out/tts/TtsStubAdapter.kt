package com.krince.reminisce.infra.adapter.out.tts

import com.krince.reminisce.application.port.out.tts.TtsPort
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(name = ["analysis.engine"], havingValue = "stub", matchIfMissing = true)
class TtsStubAdapter : TtsPort {

    companion object {
        private const val STUB_AUDIO_PREFIX = "stub://tts/"
    }

    override fun synthesize(text: String, voiceProfile: String?): String? {
        val trimmed: String = text.trim()
        if (trimmed.isBlank()) {
            return null
        }

        return "$STUB_AUDIO_PREFIX${trimmed.hashCode()}"
    }
}
