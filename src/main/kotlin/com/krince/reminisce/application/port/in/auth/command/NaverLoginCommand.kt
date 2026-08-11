package com.krince.reminisce.application.port.`in`.auth.command

class NaverLoginCommand(
    val authorizationCode: String,
    val state: String,
)
