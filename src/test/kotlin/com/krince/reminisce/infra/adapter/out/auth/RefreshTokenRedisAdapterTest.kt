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
@DisplayName("RefreshTokenRedisAdapter 통합테스트")
class RefreshTokenRedisAdapterTest(
    private val adapter: RefreshTokenRedisAdapter,
    private val redisTemplate: StringRedisTemplate,
) : FunSpec({

    beforeTest {
        redisTemplate.connectionFactory?.connection?.serverCommands()?.flushAll()
    }

    val userId = "user-uuid-1"
    val token = "Bearer refresh-token"
    val ttl = Duration.ofMinutes(10)

    context("save·find 왕복") {
        test("저장한 값을 그대로 조회하고 auth:refresh:{userId} 키에 TTL을 설정한다") {
            adapter.save(userId, token, ttl)

            adapter.find(userId) shouldBe token
            val expire = redisTemplate.getExpire("auth:refresh:$userId")
            expire.shouldNotBeNull()
            (expire in 1..600) shouldBe true
        }
    }

    context("delete") {
        test("삭제하면 조회 결과가 null이다") {
            adapter.save(userId, token, ttl)
            adapter.delete(userId)

            adapter.find(userId) shouldBe null
        }
    }

    context("미저장 조회") {
        test("저장한 적 없는 userId는 null을 반환한다") {
            adapter.find("unknown-user") shouldBe null
        }
    }
})
