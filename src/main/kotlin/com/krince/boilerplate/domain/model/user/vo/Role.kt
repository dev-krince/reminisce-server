package com.krince.boilerplate.domain.model.user.vo

import com.krince.boilerplate.shared.exception.BadRequestException
import com.krince.boilerplate.shared.response.ExceptionResponseCode.*

@JvmInline
value class Role(val value: String) {
    init {
        require(value.isNotBlank()) { throw BadRequestException(REQUIRE_NOT_BLANK, REQUIRE_NOT_BLANK.message) }
        require(value.startsWith(PREFIX)) { throw BadRequestException(REQUIRE_START_WITH, REQUIRE_START_WITH.message) }
    }

    companion object {
        private const val KOREAN_VALUE_OBJECT_NAME = "권한명"
        private const val PREFIX = "ROLE_"
        private const val ADMIN = "ADMIN"
        private const val USER = "USER"

        fun admin() = Role("$PREFIX$ADMIN")
        fun user() = Role("$PREFIX$USER")
    }
}