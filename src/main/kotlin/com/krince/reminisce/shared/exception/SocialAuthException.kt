package com.krince.reminisce.shared.exception

import com.krince.reminisce.shared.response.ExceptionResponseCode

class SocialAuthException(
    val exceptionResponseCode: ExceptionResponseCode,
    override val message: String = ExceptionResponseCode.SOCIAL_AUTH_FAILED.message,
    override val cause: Throwable? = null,
) : RuntimeException(message, cause)
