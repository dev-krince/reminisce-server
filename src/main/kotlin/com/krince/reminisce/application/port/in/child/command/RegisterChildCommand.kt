package com.krince.reminisce.application.port.`in`.child.command

class RegisterChildCommand(
    val guardianId: String,
    val nickname: String,
    val birthYear: Int,
    val consentVersion: String,
)
