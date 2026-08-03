package com.krince.reminisce.infra.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.mail")
data class MailSenderProperties(
    val from: String,
)
