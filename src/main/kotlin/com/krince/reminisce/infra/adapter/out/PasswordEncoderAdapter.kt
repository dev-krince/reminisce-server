package com.krince.reminisce.infra.adapter.out

import com.krince.reminisce.application.port.out.auth.PasswordEncoderPort
import com.krince.reminisce.shared.response.ExceptionResponseCode.*
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class PasswordEncoderAdapter(private val encoder: PasswordEncoder) : PasswordEncoderPort {
    override fun encode(password: String): String =
        encoder.encode(password) ?: throw IllegalStateException("비밀번호 암호화에 실패했습니다.")

    override fun matchPassword(rawPassword: String, encodedPassword: String) {
        encoder.matches(rawPassword, encodedPassword)
            .takeIf { it }
            ?: throw BadCredentialsException(INVALID_PASSWORD.message)
    }
}