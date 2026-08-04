package com.krince.reminisce.application.port.out.auth

import jakarta.servlet.http.HttpServletRequest
import java.time.Duration

interface TokenProviderPort {
    fun generateAccessToken(userId: String, role: String): String

    fun generateRefreshToken(userId: String, role: String): String

    fun extractToken(token: String): String

    fun validateRefreshToken(token: String)

    fun getUserId(token: String): String

    fun getTokenId(token: String): String?

    fun getRemainingExpiration(token: String): Duration

    fun getRole(token: String): String

    fun getUserIdFromRequest(request: HttpServletRequest): String?

    fun getRefreshTokenExpiration(): Duration
}
