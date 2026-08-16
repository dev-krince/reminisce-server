package com.krince.reminisce.application.port.out.tts

const val NARRATOR_VOICE_PROFILE: String = "narrator_female"

const val QUMI_VOICE_PROFILE: String = "qumi_child_friendly"

interface TtsPort {
    fun synthesize(text: String, voiceProfile: String? = null): String?
}
