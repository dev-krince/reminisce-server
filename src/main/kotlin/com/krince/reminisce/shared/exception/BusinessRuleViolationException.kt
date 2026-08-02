package com.krince.reminisce.shared.exception

import com.krince.reminisce.shared.response.ExceptionResponseCode

class BusinessRuleViolationException(
    val exceptionResponseCode: ExceptionResponseCode,
    override val message: String = ExceptionResponseCode.BUSINESS_RULE_VIOLATION.message,
) : RuntimeException(message)