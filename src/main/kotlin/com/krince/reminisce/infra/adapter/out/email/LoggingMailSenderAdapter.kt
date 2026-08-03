package com.krince.reminisce.infra.adapter.out.email

import com.krince.reminisce.application.port.out.email.MailSenderPort
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("test | localtest")
class LoggingMailSenderAdapter : MailSenderPort {
    override fun sendVerificationCode(email: String, code: String) {
    }
}
