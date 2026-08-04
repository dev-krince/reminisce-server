package com.krince.reminisce.domain.model.child.vo

import com.krince.reminisce.shared.exception.BadRequestException
import com.krince.reminisce.shared.response.ExceptionResponseCode.INVALID_CHILD_NICKNAME_LENGTH
import com.krince.reminisce.shared.response.ExceptionResponseCode.REQUIRE_NOT_BLANK

@JvmInline
value class ChildNickname(val value: String) {
    init {
        require(value.isNotBlank()) { throw BadRequestException(REQUIRE_NOT_BLANK, REQUIRE_NOT_BLANK.message) }
        require(value.length <= MAX_LENGTH) {
            throw BadRequestException(INVALID_CHILD_NICKNAME_LENGTH, INVALID_CHILD_NICKNAME_LENGTH.message)
        }
    }

    companion object {
        private const val MAX_LENGTH = 20
    }
}
