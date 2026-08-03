package com.krince.reminisce.application.port.out.auth

data class KakaoUserInfo(
    val id: String,
    val email: String?,
    val nickname: String,
)
