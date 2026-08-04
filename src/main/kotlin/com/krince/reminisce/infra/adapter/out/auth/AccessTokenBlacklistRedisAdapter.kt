package com.krince.reminisce.infra.adapter.out.auth

import com.krince.reminisce.application.port.out.auth.AccessTokenBlacklistPort
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class AccessTokenBlacklistRedisAdapter(
    private val redisTemplate: StringRedisTemplate,
) : AccessTokenBlacklistPort {

    override fun register(tokenId: String, ttl: Duration) {
        redisTemplate.opsForValue().set(blacklistKey(tokenId), BLACKLIST_MARKER, ttl)
    }

    override fun isBlacklisted(tokenId: String): Boolean =
        redisTemplate.hasKey(blacklistKey(tokenId))

    private fun blacklistKey(tokenId: String): String = "$BLACKLIST_KEY_PREFIX$tokenId"

    companion object {
        private const val BLACKLIST_KEY_PREFIX = "auth:blacklist:"
        private const val BLACKLIST_MARKER = "logout"
    }
}
