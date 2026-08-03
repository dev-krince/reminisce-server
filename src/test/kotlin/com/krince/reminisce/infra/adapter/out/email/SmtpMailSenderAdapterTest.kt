package com.krince.reminisce.infra.adapter.out.email

import com.icegreen.greenmail.util.GreenMail
import com.icegreen.greenmail.util.ServerSetup
import com.krince.reminisce.infra.config.properties.MailSenderProperties
import com.krince.reminisce.shared.exception.MailSendException
import com.krince.reminisce.shared.response.ExceptionResponseCode
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import jakarta.mail.internet.MimeMessage
import org.springframework.mail.MailSendException as SpringMailSendException
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl

private const val RECEIVE_TIMEOUT_MILLIS = 5_000L

@Tags("test", "unitTest")
@DisplayName("SmtpMailSenderAdapter 단위테스트")
class SmtpMailSenderAdapterTest : FunSpec({

    val recipientEmail = "receiver@example.com"
    val fromAddress = "no-reply@reminisce.com"
    val verificationCode = "482913"

    context("sendVerificationCode") {
        context("성공") {
            test("GreenMail이 발송한 인증 메일 1건을 수신하고 수신자·본문에 인증코드가 담긴다") {
                val serverSetup = ServerSetup.SMTP.dynamicPort()
                val greenMail = GreenMail(serverSetup)
                greenMail.start()

                try {
                    val mailSender = JavaMailSenderImpl().apply {
                        host = greenMail.smtp.bindTo
                        port = greenMail.smtp.port
                    }
                    val adapter = SmtpMailSenderAdapter(mailSender, MailSenderProperties(fromAddress))

                    adapter.sendVerificationCode(recipientEmail, verificationCode)

                    greenMail.waitForIncomingEmail(RECEIVE_TIMEOUT_MILLIS, 1) shouldBe true
                    val receivedMessages: Array<MimeMessage> = greenMail.receivedMessages
                    receivedMessages.size shouldBe 1

                    val received = receivedMessages.first()
                    received.allRecipients.first().toString() shouldBe recipientEmail
                    received.subject shouldContain "인증"
                    received.content.toString() shouldContain verificationCode
                } finally {
                    greenMail.stop()
                }
            }
        }

        context("실패") {
            val failingSender = mockk<JavaMailSender>()

            test("JavaMailSender가 MailException을 던지면 MAIL_SEND_FAILED로 매핑하고 원인을 보존한다") {
                clearMocks(failingSender)
                val cause = SpringMailSendException("smtp connect failed")
                every { failingSender.send(any<SimpleMailMessage>()) } throws cause
                val adapter = SmtpMailSenderAdapter(failingSender, MailSenderProperties(fromAddress))

                val thrown = shouldThrow<MailSendException> {
                    adapter.sendVerificationCode(recipientEmail, verificationCode)
                }

                thrown.exceptionResponseCode shouldBe ExceptionResponseCode.MAIL_SEND_FAILED
                thrown.cause shouldBe cause
            }
        }
    }
})
