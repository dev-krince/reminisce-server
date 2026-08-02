package com.krince.reminisce.shared.exception

import com.krince.reminisce.shared.response.ExceptionResponseCode

class UnauthorizedException(
    val exceptionResponseCode: ExceptionResponseCode,
    override val message: String = ExceptionResponseCode.UNAUTHORIZED.message,
) : RuntimeException(message)