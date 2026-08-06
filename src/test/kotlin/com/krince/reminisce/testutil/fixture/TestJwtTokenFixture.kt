package com.krince.reminisce.testutil.fixture

import com.krince.reminisce.infra.security.JwtProvider
import org.springframework.stereotype.Component

@Component
class TestJwtTokenFixture(
    private val jwtProvider: JwtProvider,
) {
    fun generateAccessToken(userId: String, role: String = "ROLE_USER"): String =
        jwtProvider.createAccessToken(userId, role)

    fun generateRefreshToken(userId: String, role: String = "ROLE_USER"): String =
        jwtProvider.createRefreshToken(userId, role)
}
