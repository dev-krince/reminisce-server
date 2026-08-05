package com.krince.reminisce.application.service.auth

import com.krince.reminisce.application.port.out.auth.AccessTokenBlacklistPort
import com.krince.reminisce.application.port.out.auth.TokenProviderPort
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.Duration

@Tags("test", "unitTest")
@DisplayName("AccessTokenBlacklister 단위테스트")
class AccessTokenBlacklisterTest : FunSpec({

    val tokenProviderPort = mockk<TokenProviderPort>()
    val accessTokenBlacklistPort = mockk<AccessTokenBlacklistPort>()
    val blacklister = AccessTokenBlacklister(
        tokenProviderPort = tokenProviderPort,
        accessTokenBlacklistPort = accessTokenBlacklistPort,
    )

    beforeEach { clearAllMocks() }

    val rawToken = "Bearer access-token"
    val extracted = "access-token"
    val jti = "access-jti-1"
    val remaining = Duration.ofHours(2)

    test("rawAccessToken이 null이면 register를 호출하지 않는다") {
        blacklister.blacklist(null)

        verify(exactly = 0) { accessTokenBlacklistPort.register(any(), any()) }
    }

    test("rawAccessToken이 blank이면 register를 호출하지 않는다") {
        blacklister.blacklist("   ")

        verify(exactly = 0) { accessTokenBlacklistPort.register(any(), any()) }
    }

    test("extractToken이 IllegalArgumentException을 던지면 register를 호출하지 않는다") {
        every { tokenProviderPort.extractToken(rawToken) } throws IllegalArgumentException("bad prefix")

        blacklister.blacklist(rawToken)

        verify(exactly = 0) { accessTokenBlacklistPort.register(any(), any()) }
    }

    test("getRemainingExpiration이 Duration.ZERO 이하이면 register를 호출하지 않는다") {
        every { tokenProviderPort.extractToken(rawToken) } returns extracted
        every { tokenProviderPort.getRemainingExpiration(extracted) } returns Duration.ZERO

        blacklister.blacklist(rawToken)

        verify(exactly = 0) { accessTokenBlacklistPort.register(any(), any()) }
    }

    test("getRemainingExpiration이 음수이면 register를 호출하지 않는다") {
        every { tokenProviderPort.extractToken(rawToken) } returns extracted
        every { tokenProviderPort.getRemainingExpiration(extracted) } returns Duration.ofSeconds(-1)

        blacklister.blacklist(rawToken)

        verify(exactly = 0) { accessTokenBlacklistPort.register(any(), any()) }
    }

    test("getRemainingExpiration이 RuntimeException을 던지면 register를 호출하지 않는다") {
        every { tokenProviderPort.extractToken(rawToken) } returns extracted
        every { tokenProviderPort.getRemainingExpiration(extracted) } throws
            io.jsonwebtoken.ExpiredJwtException(null, null, "expired")

        blacklister.blacklist(rawToken)

        verify(exactly = 0) { accessTokenBlacklistPort.register(any(), any()) }
    }

    test("getTokenId가 null이면 register를 호출하지 않는다") {
        every { tokenProviderPort.extractToken(rawToken) } returns extracted
        every { tokenProviderPort.getRemainingExpiration(extracted) } returns remaining
        every { tokenProviderPort.getTokenId(extracted) } returns null

        blacklister.blacklist(rawToken)

        verify(exactly = 0) { accessTokenBlacklistPort.register(any(), any()) }
    }

    test("정상 토큰이면 accessTokenBlacklistPort.register를 jti와 남은 수명으로 정확히 1회 호출한다") {
        every { tokenProviderPort.extractToken(rawToken) } returns extracted
        every { tokenProviderPort.getRemainingExpiration(extracted) } returns remaining
        every { tokenProviderPort.getTokenId(extracted) } returns jti
        every { accessTokenBlacklistPort.register(jti, remaining) } returns Unit

        blacklister.blacklist(rawToken)

        verify(exactly = 1) { accessTokenBlacklistPort.register(jti, remaining) }
    }
})
