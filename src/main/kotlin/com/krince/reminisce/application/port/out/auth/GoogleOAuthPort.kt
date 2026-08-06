package com.krince.reminisce.application.port.out.auth

interface GoogleOAuthPort {
    fun exchangeCodeForUser(authorizationCode: String): GoogleUserInfo
}
