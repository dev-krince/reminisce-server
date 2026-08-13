package com.krince.reminisce.infra.adapter.out.tts

import com.krince.reminisce.application.port.out.file.StoreFilePort
import com.krince.reminisce.application.port.out.tts.TtsPort
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
    restClientBuilder: RestClient.Builder,
) : TtsPort {

    private val restClient: RestClient = restClientBuilder.build()

    override fun synthesize(text: String, voiceProfile: String?): String? {
        val trimmed: String = text.trim()
        if (trimmed.isBlank()) {
            return null
        }
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
