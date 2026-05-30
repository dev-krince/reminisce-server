package com.krince.boilerplate.shared.exception

import com.krince.boilerplate.shared.response.ExceptionResponseCode

class BadRequestException(
    val exceptionResponseCode: ExceptionResponseCode,
    override val message: String = ExceptionResponseCode.BAD_REQUEST.message,
) : RuntimeException(message)