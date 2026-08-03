package com.krince.reminisce.application.validator.user

import com.krince.reminisce.shared.exception.BadRequestException
import com.krince.reminisce.shared.response.ExceptionResponseCode.EXPIRED_VERIFICATION_CODE
import com.krince.reminisce.shared.response.ExceptionResponseCode.INVALID_VERIFICATION_CODE

object ConfirmEmailVerificationValidator {
    fun validateCodeMatches(storedCode: String?, inputCode: String) {
        if (storedCode == null) {
            throw BadRequestException(EXPIRED_VERIFICATION_CODE, EXPIRED_VERIFICATION_CODE.message)
        }
        if (storedCode != inputCode) {
            throw BadRequestException(INVALID_VERIFICATION_CODE, INVALID_VERIFICATION_CODE.message)
        }
    }
}
