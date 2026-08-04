package com.krince.reminisce.application.validator.child

import com.krince.reminisce.shared.exception.BadRequestException
import com.krince.reminisce.shared.exception.BusinessRuleViolationException
import com.krince.reminisce.shared.response.ExceptionResponseCode.CHILD_LIMIT_EXCEEDED
import com.krince.reminisce.shared.response.ExceptionResponseCode.INVALID_BIRTH_YEAR

object RegisterChildValidator {
    fun validateWithinLimit(currentCount: Long, maxPerGuardian: Int) {
        if (currentCount >= maxPerGuardian) {
            throw BusinessRuleViolationException(CHILD_LIMIT_EXCEEDED, CHILD_LIMIT_EXCEEDED.message)
        }
    }

    fun validateBirthYearNotInFuture(birthYear: Int, currentYear: Int) {
        if (birthYear > currentYear) {
            throw BadRequestException(INVALID_BIRTH_YEAR, INVALID_BIRTH_YEAR.message)
        }
    }
}
