package com.krince.reminisce.application.port.out.email

interface MailSenderPort {
    fun sendVerificationCode(email: String, code: String)
}
