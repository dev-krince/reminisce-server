package com.krince.reminisce.application.service.admin

import com.krince.reminisce.shared.exception.ForbiddenException
import com.krince.reminisce.shared.response.ExceptionResponseCode.FORBIDDEN

object AdminKeyValidator {
    private const val ADMIN_KEY = "reminisce"

    fun verify(adminKey: String) {
        if (adminKey != ADMIN_KEY) {
            throw ForbiddenException(FORBIDDEN, FORBIDDEN.message)
        }
    }
}
