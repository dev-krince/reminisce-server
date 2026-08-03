package com.krince.reminisce.infra.adapter.out.email

import com.krince.reminisce.application.port.out.email.MailSenderPort
import com.krince.reminisce.infra.config.properties.MailSenderProperties
import com.krince.reminisce.shared.exception.MailSendException
import com.krince.reminisce.shared.response.ExceptionResponseCode
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Profile
import org.springframework.mail.MailException
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Component

@Component
@Profile("!test & !localtest")
@EnableConfigurationProperties(MailSenderProperties::class)
class SmtpMailSenderAdapter(
    private val mailSender: JavaMailSender,
    private val mailSenderProperties: MailSenderProperties,
) : MailSenderPort {

    override fun sendVerificationCode(email: String, code: String) {
        val message = SimpleMailMessage().apply {
            setFrom(mailSenderProperties.from)
            setTo(email)
            setSubject(VERIFICATION_MAIL_SUBJECT)
            setText("$VERIFICATION_CODE_TEXT_PREFIX$code")
        }

        try {
            mailSender.send(message)
        } catch (cause: MailException) {
            throw MailSendException(
                exceptionResponseCode = ExceptionResponseCode.MAIL_SEND_FAILED,
                cause = cause,
            )
        }
    }

    companion object {
        private const val VERIFICATION_MAIL_SUBJECT = "이메일 인증코드 안내"
        private const val VERIFICATION_CODE_TEXT_PREFIX = "인증코드: "
    }
}
