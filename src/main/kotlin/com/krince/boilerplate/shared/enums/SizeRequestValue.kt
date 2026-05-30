package com.krince.boilerplate.shared.enums

@JvmInline
value class SizeRequestValue(val value: Long) {
    companion object {
        private const val DEFAULT_VALUE = 10L

        val DEFAULT = SizeRequestValue(DEFAULT_VALUE).value
    }
}