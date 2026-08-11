package com.krince.reminisce.application.port.out.auth

interface NaverOAuthPort {
    fun exchangeCodeForUser(authorizationCode: String, state: String): NaverUserInfo
}
