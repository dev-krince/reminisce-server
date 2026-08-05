package com.krince.reminisce.application.service.auth

import com.krince.reminisce.application.port.out.auth.AccessTokenBlacklistPort
import com.krince.reminisce.application.port.out.auth.TokenProviderPort
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class AccessTokenBlacklister(
    private val tokenProviderPort: TokenProviderPort,
    private val accessTokenBlacklistPort: AccessTokenBlacklistPort,
) {

    fun blacklist(rawAccessToken: String?) {
        val extractedAccessToken: String = extractAccessToken(rawAccessToken) ?: return

        registerBlacklist(extractedAccessToken)
    }

    private fun extractAccessToken(rawAccessToken: String?): String? {
        val provided: String = rawAccessToken?.takeIf { it.isNotBlank() } ?: return null

        return try {
            tokenProviderPort.extractToken(provided)
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    private fun registerBlacklist(extractedAccessToken: String) {
        val remaining: Duration
        val tokenId: String
        try {
            remaining = tokenProviderPort.getRemainingExpiration(extractedAccessToken)
            if (remaining <= Duration.ZERO) return
            tokenId = tokenProviderPort.getTokenId(extractedAccessToken) ?: return
        } catch (_: RuntimeException) {
            return
        }

        accessTokenBlacklistPort.register(tokenId, remaining)
    }
}
