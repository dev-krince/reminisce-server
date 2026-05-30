package com.krince.boilerplate.shared.exception

import com.krince.boilerplate.shared.response.ExceptionResponseCode

class ForbiddenException(
    val exceptionResponseCode: ExceptionResponseCode,
    override val message: String = ExceptionResponseCode.FORBIDDEN.message
) : RuntimeException(message)