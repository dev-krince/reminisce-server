package com.krince.reminisce.infra.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "oauth.naver")
data class NaverOAuthProperties(
    val clientId: String,
    val clientSecret: String,
    val redirectUri: String,
    val tokenUri: String,
    val userInfoUri: String,
)
