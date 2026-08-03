package com.krince.reminisce.application.port.out.auth

import java.time.Duration

interface RefreshTokenPort {
    fun save(userId: String, token: String, ttl: Duration)

    fun find(userId: String): String?

    fun delete(userId: String)
}
