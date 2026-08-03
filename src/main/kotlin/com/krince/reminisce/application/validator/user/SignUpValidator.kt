package com.krince.reminisce.application.validator.user

import com.krince.reminisce.shared.exception.BadRequestException
import com.krince.reminisce.shared.exception.ConflictException
import com.krince.reminisce.shared.response.ExceptionResponseCode.DUPLICATE_EMAIL
import com.krince.reminisce.shared.response.ExceptionResponseCode.EMAIL_NOT_VERIFIED
import com.krince.reminisce.shared.response.ExceptionResponseCode.INVALID_PASSWORD_FORMAT

object SignUpValidator {
    private val PASSWORD_FORMAT =
        Regex("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z0-9\\s]).{8,}$")

    fun validateVerified(isVerified: Boolean) {
        if (!isVerified) throw BadRequestException(EMAIL_NOT_VERIFIED, EMAIL_NOT_VERIFIED.message)
    }

    fun validatePasswordFormat(rawPassword: String) {
        if (!PASSWORD_FORMAT.matches(rawPassword)) {
            throw BadRequestException(INVALID_PASSWORD_FORMAT, INVALID_PASSWORD_FORMAT.message)
        }
    }

    fun validateNotDuplicated(exists: Boolean) {
        if (exists) throw ConflictException(DUPLICATE_EMAIL, DUPLICATE_EMAIL.message)
    }
}
