package com.krince.reminisce.infra.adapter.out.tts

import com.krince.reminisce.application.port.out.file.StoreFilePort
import com.krince.reminisce.application.port.out.tts.CommandTtsCachePort
import com.krince.reminisce.application.port.out.tts.LoadTtsCachePort
import com.krince.reminisce.application.port.out.tts.TtsPort
import com.krince.reminisce.domain.model.ttscache.TtsCache
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

private val logger = KotlinLogging.logger {}

@Component
@ConditionalOnProperty(name = ["analysis.engine"], havingValue = "openai")
class TtsOpenAiAdapter(
    @Value("\${spring.ai.openai.api-key}") private val apiKey: String,
    @Value("\${tts.openai.model:gpt-4o-mini-tts}") private val model: String,
    @Value("\${tts.openai.voice:nova}") private val voice: String,
    @Value("\${tts.openai.base-url:https://api.openai.com}") private val baseUrl: String,
    private val storeFilePort: StoreFilePort,
    private val loadTtsCachePort: LoadTtsCachePort,
    private val commandTtsCachePort: CommandTtsCachePort,
    restClientBuilder: RestClient.Builder,
) : TtsPort {

    private val restClient: RestClient = restClientBuilder.build()

    override fun synthesize(text: String, voiceProfile: String?): String? {
        val trimmed: String = text.trim()
        if (trimmed.isBlank()) {
            return null
        }
        val instructions: String = voiceInstructions(voiceProfile)
        val cacheKey: String = TtsCache.cacheKey(trimmed, voiceProfile, instructions)
        loadTtsCachePort.findFileUrlByCacheKey(cacheKey)?.let { return it }

        val fileUrl: String = synthesizeAndStore(trimmed, voiceProfile, instructions) ?: return null
        commandTtsCachePort.save(TtsCache(cacheKey = cacheKey, voiceProfile = voiceProfile, fileUrl = fileUrl))

        return fileUrl
    }

    private fun synthesizeAndStore(trimmed: String, voiceProfile: String?, instructions: String): String? {
        val selectedVoice: String = mapOpenAiVoice(voiceProfile, voice)

        return runCatching {
            val audio: ByteArray? = restClient.post()
                .uri("$baseUrl$SPEECH_PATH")
                .header(AUTHORIZATION_HEADER, "$BEARER_PREFIX$apiKey")
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                    mapOf(
                        "model" to model,
                        "input" to trimmed,
                        "voice" to selectedVoice,
                        "instructions" to instructions,
                        "response_format" to AUDIO_FORMAT,
                    ),
                )
                .retrieve()
                .body(ByteArray::class.java)

            audio?.takeIf { it.isNotEmpty() }?.let { storeFilePort.saveAudioBytes(it, AUDIO_FORMAT) }
        }.getOrElse { cause ->
            logger.warn(cause) { "TTS 합성 실패 — 음성 없이 진행" }
            null
        }
    }

    companion object {
        private const val SPEECH_PATH = "/v1/audio/speech"
        private const val AUTHORIZATION_HEADER = "Authorization"
        private const val BEARER_PREFIX = "Bearer "
        private const val AUDIO_FORMAT = "mp3"
    }
}

private val EXACT_VOICE_MAP: Map<String, String> = mapOf(
    "young_woman_gentle" to "nova",
    "elderly_man_stern" to "onyx",
    "elderly_man_warm" to "echo",
)

internal fun mapOpenAiVoice(voiceProfile: String?, default: String): String {
    val profile: String = voiceProfile?.trim()?.lowercase()?.takeIf { it.isNotBlank() } ?: return default
    EXACT_VOICE_MAP[profile]?.let { return it }

    return when {
        profile.contains("woman") || profile.contains("female") || profile.contains("girl") -> "nova"
        profile.contains("man") || profile.contains("male") || profile.contains("boy") -> "onyx"
        profile.contains("child") || profile.contains("kid") -> "fable"
        else -> default
    }
}

private val VOICE_INSTRUCTIONS_MAP: Map<String, String> = mapOf(
    "narrator_female" to "당신은 따뜻한 동화 구연가입니다. 유치원 아이에게 옛날이야기를 들려주듯 생동감 있게, " +
        "장면의 분위기와 감정을 살려 또박또박 읽어 주세요.",
    "qumi_child_friendly" to "당신은 밝고 다정한 어린이 친구 로봇 '큐미'입니다. 호기심과 장난기가 느껴지는 " +
        "통통 튀는 말투로, 아이에게 말을 거는 것처럼 자연스럽게 말해 주세요.",
    "young_woman_gentle" to "당신은 동화 속 젊은 며느리입니다. 부드럽고 조심스러운 말투로, " +
        "걱정·부끄러움·기쁨 같은 감정이 목소리에 자연스럽게 묻어나게 연기해 주세요.",
    "elderly_man_stern" to "당신은 동화 속 근엄한 시아버지입니다. 나이 든 남성의 묵직한 말투로, " +
        "놀람과 화남 같은 감정을 실감 나게 연기해 주세요.",
    "elderly_man_warm" to "당신은 동화 속 푸근한 이장님입니다. 나이 든 남성의 따뜻하고 정감 있는 말투로, " +
        "웃음기와 고마움이 느껴지게 연기해 주세요.",
)

private const val DEFAULT_VOICE_INSTRUCTIONS: String =
    "아이에게 들려주는 동화 음성입니다. 자연스럽고 생기 있게, 감정을 살려 말해 주세요."

internal fun voiceInstructions(voiceProfile: String?): String {
    val profile: String = voiceProfile?.trim()?.lowercase()?.takeIf { it.isNotBlank() }
        ?: return DEFAULT_VOICE_INSTRUCTIONS

    return VOICE_INSTRUCTIONS_MAP[profile] ?: DEFAULT_VOICE_INSTRUCTIONS
}
