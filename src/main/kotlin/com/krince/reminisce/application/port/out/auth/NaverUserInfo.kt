package com.krince.reminisce.application.port.out.auth

data class NaverUserInfo(
    val id: String,
    val email: String?,
    val nickname: String,
)
