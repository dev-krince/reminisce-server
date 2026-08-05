package com.krince.reminisce.domain.model.speakingsession.vo

import com.krince.reminisce.shared.exception.BadRequestException
import com.krince.reminisce.shared.response.ExceptionResponseCode.REQUIRE_NOT_BLANK

@JvmInline
value class SpeakingSessionId(val value: String) {
    init {
        require(value.isNotBlank()) { throw BadRequestException(REQUIRE_NOT_BLANK, REQUIRE_NOT_BLANK.message) }
    }
}
