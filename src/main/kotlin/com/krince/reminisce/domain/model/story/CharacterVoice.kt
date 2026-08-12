package com.krince.reminisce.domain.model.story

enum class VoiceGender {
    FEMALE,
    MALE,
}

enum class VoiceAgeGroup {
    CHILD,
    ADULT,
    ELDER,
}

data class CharacterVoice(
    val gender: VoiceGender,
    val ageGroup: VoiceAgeGroup,
    val voiceProfile: String,
)
