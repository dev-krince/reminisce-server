package com.krince.reminisce.domain.model.ttscache

import java.security.MessageDigest

class TtsCache(
    val cacheKey: String,
    val voiceProfile: String?,
    val fileUrl: String,
) {
    companion object {
        private const val DIGEST_ALGORITHM = "SHA-256"
        private const val KEY_SEPARATOR = " "
        private const val HEX_FORMAT = "%02x"

        fun cacheKey(text: String, voiceProfile: String?, style: String? = null): String {
            val normalizedVoice: String = voiceProfile?.trim()?.lowercase().orEmpty()
            val normalizedStyle: String = style?.trim().orEmpty()
            val normalizedText: String = text.trim()
            val source: String =
                "${normalizedVoice.length}$KEY_SEPARATOR$normalizedVoice" +
                    "$KEY_SEPARATOR${normalizedStyle.length}$KEY_SEPARATOR$normalizedStyle" +
                    "$KEY_SEPARATOR$normalizedText"

            return MessageDigest.getInstance(DIGEST_ALGORITHM)
                .digest(source.toByteArray(Charsets.UTF_8))
                .joinToString(separator = "") { HEX_FORMAT.format(it) }
        }
    }
}
