package com.krince.reminisce.shared.exception

import com.krince.reminisce.shared.response.ExceptionResponseCode

class MailSendException(
    val exceptionResponseCode: ExceptionResponseCode,
    override val message: String = ExceptionResponseCode.MAIL_SEND_FAILED.message,
    override val cause: Throwable? = null,
) : RuntimeException(message, cause)
