package com.krince.reminisce.infra.adapter.out.email

import com.krince.reminisce.application.port.out.email.MailSenderPort
import org.springframework.stereotype.Component

@Component
class LoggingMailSenderAdapter : MailSenderPort {
    override fun sendVerificationCode(email: String, code: String) {
    }
}
