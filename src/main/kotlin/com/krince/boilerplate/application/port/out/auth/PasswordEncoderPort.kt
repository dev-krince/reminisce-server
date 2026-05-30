package com.krince.boilerplate.application.port.out.auth

interface PasswordEncoderPort {
    fun encode(password: String): String
    fun matchPassword(rawPassword: String, encodedPassword: String)
}