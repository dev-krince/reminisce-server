package com.krince.boilerplate.shared.exception

import com.krince.boilerplate.shared.response.ExceptionResponseCode

class BusinessRuleViolationException(
    val exceptionResponseCode: ExceptionResponseCode,
    override val message: String = ExceptionResponseCode.BUSINESS_RULE_VIOLATION.message,
) : RuntimeException(message)