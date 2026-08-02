package com.krince.reminisce.shared.exception

import com.krince.reminisce.shared.response.ExceptionResponseCode

class UnauthorizedRefreshTokenException(
    val exceptionResponseCode: ExceptionResponseCode,
    override val message: String = ExceptionResponseCode.UNAUTHORIZED_REFRESH_TOKEN.message,
) : RuntimeException(message)