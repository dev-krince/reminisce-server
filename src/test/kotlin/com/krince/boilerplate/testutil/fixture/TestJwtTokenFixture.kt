package com.krince.boilerplate.testutil.fixture

import com.krince.boilerplate.infra.security.JwtProvider
import org.springframework.stereotype.Component

@Component
class TestJwtTokenFixture(
    private val jwtProvider: JwtProvider,
) {
    fun generateAccessToken(userId: String, role: String = "ROLE_USER"): String =
        jwtProvider.createAccessToken(userId, role)
}
