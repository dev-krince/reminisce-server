package com.krince.reminisce.domain.model.user.vo

import com.krince.reminisce.shared.exception.BadRequestException
import com.krince.reminisce.shared.response.ExceptionResponseCode.INVALID_EMAIL_FORMAT
import com.krince.reminisce.shared.response.ExceptionResponseCode.REQUIRE_NOT_BLANK

@JvmInline
value class Email(val value: String) {
    init {
        require(value.isNotBlank()) { throw BadRequestException(REQUIRE_NOT_BLANK, REQUIRE_NOT_BLANK.message) }
        require(EMAIL_FORMAT.matches(value)) { throw BadRequestException(INVALID_EMAIL_FORMAT, INVALID_EMAIL_FORMAT.message) }
    }

    companion object {
        private val EMAIL_FORMAT = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    }
}
