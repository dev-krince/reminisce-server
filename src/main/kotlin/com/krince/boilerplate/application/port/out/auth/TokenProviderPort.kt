package com.krince.boilerplate.application.port.out.auth

import jakarta.servlet.http.HttpServletRequest

interface TokenProviderPort {
    fun generateAccessToken(userId: String, role: String): String

    fun generateRefreshToken(userId: String, role: String): String

    fun extractToken(token: String): String

    fun validateRefreshToken(token: String)

    fun getUserId(token: String): String

    fun getRole(token: String): String

    fun getUserIdFromRequest(request: HttpServletRequest): String?
}