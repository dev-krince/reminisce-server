package com.krince.boilerplate.shared.exception

import com.krince.boilerplate.shared.response.ExceptionResponseCode

class UnauthorizedRefreshTokenException(
    val exceptionResponseCode: ExceptionResponseCode,
    override val message: String = ExceptionResponseCode.UNAUTHORIZED_REFRESH_TOKEN.message,
) : RuntimeException(message)