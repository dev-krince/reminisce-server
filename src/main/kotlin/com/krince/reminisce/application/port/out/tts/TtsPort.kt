package com.krince.reminisce.application.port.out.tts

interface TtsPort {
    fun synthesize(text: String, voiceProfile: String? = null): String?
}
