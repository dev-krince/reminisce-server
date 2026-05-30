package com.krince.boilerplate.shared.exception

import com.krince.boilerplate.shared.response.ExceptionResponseCode

class NotFoundException(
    val exceptionResponseCode: ExceptionResponseCode,
    override val message: String = ExceptionResponseCode.NOT_FOUND.message,
) : RuntimeException(message)