package com.krince.reminisce.application.port.out.auth

import java.time.Duration

interface AccessTokenBlacklistPort {
    fun register(tokenId: String, ttl: Duration)

    fun isBlacklisted(tokenId: String): Boolean
}
