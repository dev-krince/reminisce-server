package com.krince.reminisce.domain.model.user.vo

import com.krince.reminisce.shared.exception.BadRequestException
import com.krince.reminisce.shared.response.ExceptionResponseCode.INVALID_NICKNAME_LENGTH
import com.krince.reminisce.shared.response.ExceptionResponseCode.REQUIRE_NOT_BLANK

@JvmInline
value class Nickname(val value: String) {
    init {
        require(value.isNotBlank()) { throw BadRequestException(REQUIRE_NOT_BLANK, REQUIRE_NOT_BLANK.message) }
        require(value.length in MIN_LENGTH..MAX_LENGTH) {
            throw BadRequestException(INVALID_NICKNAME_LENGTH, INVALID_NICKNAME_LENGTH.message)
        }
    }

    companion object {
        private const val MIN_LENGTH = 2
        private const val MAX_LENGTH = 20
    }
}
