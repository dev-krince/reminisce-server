package com.krince.reminisce.application.validator.auth

import com.krince.reminisce.shared.exception.UnauthorizedRefreshTokenException
import com.krince.reminisce.shared.response.ExceptionResponseCode.INVALID_REFRESH_TOKEN

object ReissueTokenValidator {
    fun validateMatches(providedToken: String, storedToken: String?) {
        if (storedToken == null) {
            throw UnauthorizedRefreshTokenException(INVALID_REFRESH_TOKEN, INVALID_REFRESH_TOKEN.message)
        }
        if (storedToken != providedToken) {
            throw UnauthorizedRefreshTokenException(INVALID_REFRESH_TOKEN, INVALID_REFRESH_TOKEN.message)
        }
    }
}
