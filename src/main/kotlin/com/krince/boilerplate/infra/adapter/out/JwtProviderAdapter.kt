package com.krince.boilerplate.infra.adapter.out

import com.krince.boilerplate.application.port.out.auth.TokenProviderPort
import com.krince.boilerplate.infra.security.JwtProvider
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Component

@Component
class JwtProviderAdapter(private val jwtProvider: JwtProvider) : TokenProviderPort {
    override fun generateAccessToken(userId: String, role: String): String =
        jwtProvider.createAccessToken(id = userId, role = role)

    override fun generateRefreshToken(userId: String, role: String): String =
        jwtProvider.createRefreshToken(role = role, id = userId)

    override fun extractToken(token: String): String = jwtProvider.extractToken(token)

    override fun validateRefreshToken(token: String) = jwtProvider.validateRefreshToken(token)

    override fun getUserId(token: String): String = jwtProvider.getId(token)

    override fun getRole(token: String): String = jwtProvider.getRole(token)

    override fun getUserIdFromRequest(request: HttpServletRequest): String? = jwtProvider.getUserIdFromRequest(request)
}