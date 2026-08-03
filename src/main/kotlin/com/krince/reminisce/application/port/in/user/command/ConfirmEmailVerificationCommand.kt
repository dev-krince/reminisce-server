package com.krince.reminisce.application.port.`in`.user.command

class ConfirmEmailVerificationCommand(
    val email: String,
    val code: String,
)
