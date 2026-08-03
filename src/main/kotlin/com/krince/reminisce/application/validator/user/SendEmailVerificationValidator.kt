package com.krince.reminisce.application.validator.user

import com.krince.reminisce.shared.exception.ConflictException
import com.krince.reminisce.shared.response.ExceptionResponseCode.DUPLICATE_EMAIL

object SendEmailVerificationValidator {
    fun validateNotDuplicated(exists: Boolean) {
        if (exists) throw ConflictException(DUPLICATE_EMAIL, DUPLICATE_EMAIL.message)
    }
}
