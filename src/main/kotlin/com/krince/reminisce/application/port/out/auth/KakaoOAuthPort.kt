package com.krince.reminisce.application.port.out.auth

interface KakaoOAuthPort {
    fun exchangeCodeForUser(authorizationCode: String): KakaoUserInfo
}
