package com.krince.reminisce.infra.adapter.out.email

import com.krince.reminisce.application.port.out.email.EmailVerificationPort
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class EmailVerificationRedisAdapter(
    private val redisTemplate: StringRedisTemplate,
) : EmailVerificationPort {

    override fun saveCode(email: String, code: String, ttl: Duration) {
        redisTemplate.opsForValue().set(codeKey(email), code, ttl)
    }

    override fun findCode(email: String): String? = redisTemplate.opsForValue().get(codeKey(email))

    override fun markVerified(email: String) {
        redisTemplate.opsForValue().set(verifiedKey(email), VERIFIED_VALUE, VERIFIED_TTL)
    }

    override fun isVerified(email: String): Boolean =
        redisTemplate.opsForValue().get(verifiedKey(email)) == VERIFIED_VALUE

    override fun deleteCode(email: String) {
        redisTemplate.delete(codeKey(email))
    }

    private fun codeKey(email: String): String = "$CODE_KEY_PREFIX$email"

    private fun verifiedKey(email: String): String = "$VERIFIED_KEY_PREFIX$email"

    companion object {
        private const val CODE_KEY_PREFIX = "email:verification:"
        private const val VERIFIED_KEY_PREFIX = "email:verified:"
        private const val VERIFIED_VALUE = "true"
        private val VERIFIED_TTL: Duration = Duration.ofMinutes(30)
    }
}
