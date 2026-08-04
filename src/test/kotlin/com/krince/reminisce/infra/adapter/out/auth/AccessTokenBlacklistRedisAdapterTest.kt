package com.krince.reminisce.infra.adapter.out.auth

import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.test.context.ActiveProfiles
import java.time.Duration

@SpringBootTest
@ActiveProfiles("localtest")
@Tags("test", "integrationTest")
@DisplayName("AccessTokenBlacklistRedisAdapter 통합테스트")
class AccessTokenBlacklistRedisAdapterTest(
    private val adapter: AccessTokenBlacklistRedisAdapter,
    private val redisTemplate: StringRedisTemplate,
) : FunSpec({

    beforeTest {
        redisTemplate.connectionFactory?.connection?.serverCommands()?.flushAll()
    }

    val tokenId = "access-jti-1"
    val ttl = Duration.ofMinutes(10)

    context("register·isBlacklisted 왕복") {
        test("등록한 jti는 isBlacklisted가 true이고 auth:blacklist:{jti} 키에 TTL을 설정한다") {
            adapter.register(tokenId, ttl)

            adapter.isBlacklisted(tokenId) shouldBe true
            val expire = redisTemplate.getExpire("auth:blacklist:$tokenId")
            expire.shouldNotBeNull()
            (expire in 1L..ttl.seconds) shouldBe true
        }
    }

    context("미등록 조회") {
        test("등록한 적 없는 jti는 isBlacklisted가 false이다") {
            adapter.isBlacklisted("unknown-jti") shouldBe false
        }
    }
})
