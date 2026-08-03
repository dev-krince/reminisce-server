package com.krince.reminisce.application.port.out.email

import java.time.Duration

interface EmailVerificationPort {
    fun saveCode(email: String, code: String, ttl: Duration)

    fun findCode(email: String): String?

    fun markVerified(email: String)

    fun isVerified(email: String): Boolean

    fun deleteCode(email: String)
}
