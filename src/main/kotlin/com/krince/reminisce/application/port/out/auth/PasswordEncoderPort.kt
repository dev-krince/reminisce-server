package com.krince.reminisce.application.port.out.auth

interface PasswordEncoderPort {
    fun encode(password: String): String
    fun matchPassword(rawPassword: String, encodedPassword: String)
    fun matchDummyPassword(rawPassword: String)
}