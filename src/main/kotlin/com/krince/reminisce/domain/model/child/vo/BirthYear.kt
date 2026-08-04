package com.krince.reminisce.domain.model.child.vo

import com.krince.reminisce.shared.exception.BadRequestException
import com.krince.reminisce.shared.response.ExceptionResponseCode.INVALID_BIRTH_YEAR

@JvmInline
value class BirthYear(val value: Int) {
    init {
        require(value in MIN_YEAR..MAX_YEAR) {
            throw BadRequestException(INVALID_BIRTH_YEAR, INVALID_BIRTH_YEAR.message)
        }
    }

    companion object {
        private const val MIN_YEAR = 1900
        private const val MAX_YEAR = 9999
    }
}
