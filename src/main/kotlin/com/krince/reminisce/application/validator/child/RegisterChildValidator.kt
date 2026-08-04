package com.krince.reminisce.application.validator.child

import com.krince.reminisce.shared.exception.BusinessRuleViolationException
import com.krince.reminisce.shared.response.ExceptionResponseCode.CHILD_LIMIT_EXCEEDED

object RegisterChildValidator {
    fun validateWithinLimit(currentCount: Long, maxPerGuardian: Int) {
        if (currentCount >= maxPerGuardian) {
            throw BusinessRuleViolationException(CHILD_LIMIT_EXCEEDED, CHILD_LIMIT_EXCEEDED.message)
        }
    }
}
