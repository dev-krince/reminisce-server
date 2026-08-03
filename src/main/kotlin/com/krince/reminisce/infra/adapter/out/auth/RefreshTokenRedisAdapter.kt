package com.krince.reminisce.infra.adapter.out.auth

import com.krince.reminisce.application.port.out.auth.RefreshTokenPort
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class RefreshTokenRedisAdapter(
    private val redisTemplate: StringRedisTemplate,
) : RefreshTokenPort {

    override fun save(userId: String, token: String, ttl: Duration) {
        redisTemplate.opsForValue().set(refreshKey(userId), token, ttl)
    }

    override fun find(userId: String): String? = redisTemplate.opsForValue().get(refreshKey(userId))

    override fun delete(userId: String) {
        redisTemplate.delete(refreshKey(userId))
    }

    private fun refreshKey(userId: String): String = "$REFRESH_KEY_PREFIX$userId"

    companion object {
        private const val REFRESH_KEY_PREFIX = "auth:refresh:"
    }
}
